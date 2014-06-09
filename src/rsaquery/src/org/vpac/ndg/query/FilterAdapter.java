/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.ndg.query;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.QueryDefinition.AttributeDefinition;
import org.vpac.ndg.query.coordinates.HasBounds;
import org.vpac.ndg.query.coordinates.HasRank;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Swizzle;
import org.vpac.ndg.query.math.SwizzleFactory;
import org.vpac.ndg.query.math.Type;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.CellFactory;
import org.vpac.ndg.query.sampling.HasDimensions;
import org.vpac.ndg.query.sampling.HasPrototype;
import org.vpac.ndg.query.sampling.NodataNullStrategy;
import org.vpac.ndg.query.sampling.NodataStrategy;
import org.vpac.ndg.query.sampling.PixelSource;
import org.vpac.ndg.query.sampling.PixelSourceFactory;
import org.vpac.ndg.query.sampling.PixelSourceScalar;
import org.vpac.ndg.query.sampling.PixelSourceVector;
import org.vpac.ndg.query.sampling.Prototype;
import org.vpac.ndg.query.sampling.SwizzledPixelScalar;
import org.vpac.ndg.query.sampling.SwizzledPixelVector;

/**
 * A node in a query graph. This class wraps {@link Filter Filters}, providing
 * the extra functionality needed by the query engine.
 * 
 * @author Alex Fraser
 */
public class FilterAdapter implements HasBounds, HasRank, Diagnostics {

	private final Logger log = LoggerFactory.getLogger(FilterAdapter.class);

	private FilterDebug d;
	private BoxReal bounds;
	private String[] dimensions;
	private String name;
	private Filter innerFilter;
	private VectorReal internalCo;
	private VectorReal lastCo;
	private Map<String, PixelSource> outputSockets;

	CellFactory cellFactory;
	PixelSourceFactory pixelSourceFactory;

	public FilterAdapter(String name, Filter innerFilter) throws
			QueryConfigurationException {

		this.name = name;
		this.innerFilter = innerFilter;
		d = new FilterDebug(this);
		outputSockets = new HashMap<String, PixelSource>();
		cellFactory = new CellFactory();
		pixelSourceFactory = new PixelSourceFactory();
	}

	public void initialise() throws QueryConfigurationException {
		innerFilter.initialise(bounds);
	}

	Cell createCell(String name) throws QueryConfigurationException {
		Field f;
		try {
			f = innerFilter.getClass().getField(name);
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
					"Could not create output socket %s in filter %s",
					name, innerFilter.getClass()), e);
		}

		CellType cellType = f.getAnnotation(CellType.class);
		if (cellType == null) {
			throw new QueryConfigurationException(String.format(
					"Could not create output socket %s in filter %s: no type " +
					"defined (missing @CellType annotation).",
					name, innerFilter.getClass()));
		}

		Prototype prototype;
		try {
			prototype = getPrototype(cellType.value()).copy();
		} catch (QueryConfigurationException e) {
			throw new QueryConfigurationException(String.format(
					"Could not create output socket %s in filter %s: %s", name,
					innerFilter.getClass(), e.getMessage()), e);
		}

		String convertTo = cellType.as();
		prototype.convert(convertTo);

		log.debug("Creating cell {} with prototype {}", name, prototype);
		return cellFactory.create(name, prototype);
	}

	Prototype getPrototype(String inheritDecl)
			throws QueryConfigurationException {

		if (inheritDecl.contains(",")) {
			// Vector
			String[] componentDecls = inheritDecl.split(",");
			Prototype[] ps = new Prototype[componentDecls.length];
			for (int i = 0; i < ps.length; i++) {
				ps[i] = getPrototypeSingle(componentDecls[i]);
			}
			return Prototype.combine(ps, dimensions);
		} else {
			Prototype pt = getPrototypeSingle(inheritDecl).copy();
			pt.setDimensions(dimensions);
			return pt;
		}
	}

	Prototype getPrototypeSingle(String typeDecl)
			throws QueryConfigurationException {

		Type type;
		try {
			type = Type.get(typeDecl);
		} catch (IllegalArgumentException e) {
			type = null;
		}

		if (type != null) {
			// Explicit type.
			// TODO: allow nodata strategy to be specified in type declaration
			Type[] types = new Type[] { type };
			NodataStrategy nds = new NodataNullStrategy(type);
			NodataStrategy[] ndss = new NodataStrategy[] { nds };
			AttributeDefinition[][] attrs = new AttributeDefinition[][] { new AttributeDefinition[0] };
			return new Prototype(types, ndss, attrs, dimensions);
		}

		// Inherited type; may be scalar or vector
		Field tf;
		try {
			tf = innerFilter.getClass().getField(typeDecl);
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
					"Could not find field %s to inherit type from.", typeDecl),
					e);
		}
		if (!HasPrototype.class.isAssignableFrom(tf.getType())) {
			throw new QueryConfigurationException(String.format(
					"Inherited field %s has no prototype.", typeDecl));
		}

		HasPrototype typeSource;
		try {
			if (tf.get(innerFilter) == null) {
				throw new QueryConfigurationException(String.format(
						"Inherited field %s accessed before assignment.",
						typeDecl));
			}
			typeSource = (HasPrototype) tf.get(innerFilter);
		} catch (IllegalAccessException e) {
			throw new QueryConfigurationException(String.format(
					"Inherited field %s is not accessible.", typeDecl));
		}

		return typeSource.getPrototype();
	}

	/**
	 * Get an output socket. Note that this must be called after the shape of
	 * the filter has been set.
	 *
	 * @param name The name of the output socket.
	 * @return A sampler that fetches filtered values from this filter.
	 * @throws QueryConfigurationException if the field does not exist, or if it
	 *             is inaccessible.
	 */
	public PixelSource getOutputSocket(String name)
			throws QueryConfigurationException {

		PixelSource socket = outputSockets.get(name);

		if (socket != null)
			return socket;

		Cell cell = createCell(name);
		socket = pixelSourceFactory.create(this, cell, bounds);
		log.debug("Created output socket {}", socket);

		Field f;
		try {
			f = innerFilter.getClass().getField(name);
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
					"Could not create output socket %s in filter %s",
					name, innerFilter.getClass()), e);
		}
		try {
			f.set(innerFilter, cell);
		} catch (IllegalAccessException e) {
			throw new QueryConfigurationException(String.format(
					"Could not assign output socket %s in filter %s",
					name, innerFilter.getClass()), e);
		}

		outputSockets.put(name, socket);

		return socket;
	}

	/**
	 * Evaluate the filter; i.e. ask the filter to populate its outputs for a
	 * given coordinate. If the filter has already been run for those
	 * coordinates, cached values may be used for the outputs, in which case the
	 * kernel will not be called.
	 * @param co The coordinates in the output image being written to.
	 * @throws IOException If the filter's source fields could not be read from.
	 */
	public void invoke(VectorReal co) throws IOException {
		try {
			if (co.equals(lastCo))
				return;
			internalCo.set(co);
		} catch (IndexOutOfBoundsException e) {
			throw new QueryRuntimeException(String.format(
					"Failed to set coordinates of filter \"%s\". Check " +
					"dimensionality.", name), e);
		}
		innerFilter.kernel(internalCo);
		lastCo.set(co);
	}

	@Override
	public BoxReal getBounds() {
		return bounds;
	}

	/**
	 * Helper class for processing grouped constraints (where fields are
	 * co-dependent).
	 * @author Alex Fraser
	 */
	private static class GroupImpl implements HasRank, HasDimensions, HasBounds {
		static final Logger log = LoggerFactory.getLogger(GroupImpl.class);

		String name;
		Filter filter;
		FilterDebug d;

		Collection<Field> members;
		// Lower and upper bounds of the members in the group.
		int rankLower;
		int rankUpper;
		Field rankLowerField;
		Field rankUpperField;

		int intrinsicMax;
		String[] dimensions;
		BoxReal bounds;

		GroupImpl(String name, FilterAdapter context) {
			this.name = name;
			this.filter = context.getInnerFilter();
			d = new FilterDebug(context);

			members = new ArrayList<Field>();
			// Lower and upper bounds of the members in the group.
			rankLower = Integer.MIN_VALUE;
			rankUpper = Integer.MAX_VALUE;
			rankLowerField = null;
			rankUpperField = null;

			intrinsicMax = Integer.MIN_VALUE;

			dimensions = null;
		}

		void add(Field field) throws QueryConfigurationException {
			Rank rank = field.getAnnotation(Rank.class);
			if (rank == null)
				rank = getDefaultRankConstraint();

			foldClassConstraints(field, rank);

			PixelSource source = getValue(field);
			foldInstanceConstraints(field, source, rank);
			members.add(field);
		}

		private PixelSource getValue(Field field)
				throws QueryConfigurationException {
			PixelSource source;
			try {
				source = (PixelSource) field.get(filter);
			} catch (IllegalAccessException e) {
				throw new QueryConfigurationException(String.format(
						"Could not access reduction field %s.",
						d.memberStr(field)), e);
			}
			if (source == null) {
				throw new QueryConfigurationException(String.format(
						"Field %s has not been attached to an input.",
						d.pathStr(field)));
			}
			return source;
		}

		private void foldClassConstraints(Field field, Rank rank)
				throws QueryConfigurationException {

			// Lower-bound starts very small, and grows as each new constraint
			// is considered. Vice-versa for upper bound.
			if (rank.lowerBound() >= 0 && rank.lowerBound() > rankLower) {
				rankLower = rank.lowerBound();
				rankLowerField = field;
			}
			if (rank.upperBound() >= 0 && rank.upperBound() < rankUpper) {
				rankUpper = rank.upperBound();
				rankUpperField = field;
			}
			if (rank.is() >= 0) {
				if (rankLower > rank.is()) {
					throw new QueryConfigurationException(String.format(
							"Rank constraints on %s can't be met: `is`"
							+ " parameter is less than group \"%s\" lower"
							+ " bound. The lower bound was set by %s.",
							d.memberStr(field), this.name,
							d.memberStr(rankUpperField)));
				}
				if (rankUpper < rank.is()) {
					throw new QueryConfigurationException(String.format(
							"Rank constraints on %s can't be met: `is`"
							+ " parameter is greater than group \"%s\" upper"
							+ " bound. The upper bound was set by %s.",
							d.memberStr(field), this.name,
							d.memberStr(rankUpperField)));
				}
				rankLower = rankUpper = rank.is();
				rankLowerField = rankUpperField = field;
			}
			if (rankUpper < rankLower) {
				throw new QueryConfigurationException(String.format(
						"Rank constraints on %s (group %s) can't be met: lower"
						+ " bound is greater than upper bound in group %s.",
						d.memberStr(field), this.name));
			}
		}

		private void foldInstanceConstraints(Field field, PixelSource source,
				Rank rank) throws QueryConfigurationException {

			if (source.getRank() < rankLower) {
				throw new QueryConfigurationException(String.format(
						"Field %s can't have fewer dimensions than %s. Path is"
						+ " %s.",
						d.memberStr(field), d.memberStr(rankLowerField),
						d.pathStr(field)));
			}
			if (source.getRank() > rankUpper) {
				throw new QueryConfigurationException(String.format(
						"Field %s can't have more dimensions than %s. Path is"
						+ " %s",
						d.memberStr(field), d.memberStr(rankUpperField),
						d.pathStr(field)));
			}
			if (!rank.demote() && source.getRank() > rankLower) {
				rankLower = source.getRank();
				rankLowerField = field;
			}
			if (!rank.promote() && source.getRank() < rankUpper) {
				rankUpper = source.getRank();
				rankUpperField = field;
			}

			// Soft constraints.
			if (source.getRank() > intrinsicMax)
				intrinsicMax = source.getRank();
		}

		/**
		 * Provide defaults for annotations :/
		 */
		@Rank
		public PixelSource _dummyField;
		private Rank getDefaultRankConstraint() {
			Field field;
			try {
				field = GroupImpl.class.getField("_dummyField");
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(
						"Failed to access dummy field. Should be here!", e);
			}
			return field.getAnnotation(Rank.class);
		}

		void coerce() throws QueryConfigurationException {
			int targetRank = getRank();

			String[] effectiveDims = null;
			Swizzle swizzle = SwizzleFactory.resize(targetRank, targetRank);
			for (Field field : members) {
				PixelSource source = getValue(field);
				if (source.getRank() >= targetRank) {
					String[] dims = new String[targetRank];
					swizzle.swizzle(source.getPrototype().getDimensions(), dims);
					if (effectiveDims == null)
						effectiveDims = dims;
					else if (!Arrays.equals(effectiveDims, dims));
						
				}
			}

			for (Field field : members) {
				PixelSource source = getValue(field);
				if (source.getRank() == targetRank)
					continue;

				String message;
				if (source.getRank() > targetRank)
					message = "Demoting dimensionality of {} to rank {}.";
				else
					message = "Promoting dimensionality of {} to rank {}.";
				log.info(message, d.pathStr(field), targetRank);

				swizzle = SwizzleFactory.resize(
						source.getRank(), targetRank);
				PixelSource wrap;
				if (PixelSourceScalar.class.isAssignableFrom(source.getClass())) {
					wrap = new SwizzledPixelScalar((PixelSourceScalar) source,
							swizzle, source.getRank(), targetRank);
				} else {
					wrap = new SwizzledPixelVector((PixelSourceVector) source,
							swizzle, source.getRank(), targetRank);
				}
				try {
					field.set(filter, wrap);
				} catch (IllegalAccessException e) {
					throw new QueryConfigurationException(String.format(
							"Could not set field %s.", d.memberStr(field)), e);
				}
			}

			dimensions = null;
			Field dimsField = null;
			for (Field field : members) {
				PixelSource source = getValue(field);
				swizzle = SwizzleFactory.resize(source.getRank(), targetRank);
				if (source.getRank() >= targetRank) {
					// Use this source for the dimension names.
					String[] dims = new String[targetRank];
					swizzle.swizzle(source.getPrototype().getDimensions(), dims);
					if (effectiveDims == null) {
						effectiveDims = dims;
						dimsField = field;
					} else if (!Arrays.equals(effectiveDims, dims)) {
						throw new QueryConfigurationException(String.format(
								"Could not determine dimensions of group %s:"
								+ " can't decide between candidates %s"
								+ " specified by %s and %s specified by %s.",
								this.name, effectiveDims, dimsField,
								dims, field));
					}
				}
			}

			bounds = new BoxReal(targetRank);
			for (Field field : members) {
				PixelSource source = getValue(field);
				bounds.unionIfPositive(source.getBounds());
			}
		}

		/**
		 * @return The effective rank of this group. If there is a choice then
		 *         the rank will be promoted up to the maximum rank of all
		 *         members of the group.
		 */
		@Override
		public int getRank() {
			return Math.min(intrinsicMax, rankUpper);
		}

		@Override
		public String[] getDimensions() {
			return dimensions;
		}

		@Override
		public BoxReal getBounds() {
			return bounds;
		}

	}

	public void applyInputConstraints() throws QueryConfigurationException {

		Map<String, GroupImpl> groups = new HashMap<String, GroupImpl>();

		// This is a two-step process:
		// 1. Gather all constraints for each group. This means iterating over
		//    all public fields.
		// 2. <See below>
		for (Field field : innerFilter.getClass().getFields()) {
			if ((field.getModifiers() | Modifier.PUBLIC) == 0)
				continue;

			if (!PixelSource.class.isAssignableFrom(field.getType()))
				continue;

			String groupName = "";
			Rank rankConstraint = field.getAnnotation(Rank.class);
			if (rankConstraint != null)
				groupName = field.getAnnotation(Rank.class).group();
			if ("".equals(groupName))
				groupName = field.getName();

			GroupImpl group = groups.get(groupName);
			if (group == null) {
				group = new GroupImpl(groupName, this);
				groups.put(groupName, group);
			}
			group.add(field);
		}

		// 1. <See above>
		// 2. Coerce inputs to fit constraints, where the constraints are
		//    lenient (e.g. where dimensional promotion is allowed).
		for (GroupImpl group : groups.values()) {
			group.coerce();
		}
	}

	/**
	 * Finds the effective shape of this filter by applying a reduction to one
	 * of the inputs.
	 */
	void inferShapeFromInputs() throws QueryConfigurationException {
		InheritDimensions reduces = innerFilter.getClass().getAnnotation(
				InheritDimensions.class);

		if (reduces == null) {
			throw new QueryConfigurationException(String.format(
					"Could not determine dimensionality of filter %s. Class "
					+ "should be annotated with @InheritDimensions.",
					innerFilter.getClass().getName()));
		}

		Field field;
		try {
			field = innerFilter.getClass().getField(reduces.from());
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
					"Could not find reduction field %s.",
					d.memberStr(reduces.from())));
		}

		Object value;
		try {
			value = field.get(innerFilter);
		} catch (IllegalAccessException e) {
			throw new QueryConfigurationException(String.format(
					"Could not access reduction field %s.",
					d.memberStr(reduces.from())));
		}
		if (value == null) {
			throw new QueryConfigurationException(String.format(
					"Reduction field %s is null. Path is %s.",
					d.memberStr(reduces.from()), d.pathStr(reduces.from())));
		}
		if (!HasBounds.class.isAssignableFrom(value.getClass())) {
			throw new QueryConfigurationException(String.format(
					"Reduction field %s has no bounds. Path is %s.",
					d.memberStr(reduces.from()), d.pathStr(reduces.from())));
		}
		if (!HasPrototype.class.isAssignableFrom(value.getClass())) {
			throw new QueryConfigurationException(String.format(
					"Reduction field %s has no prototype. Path is %s.",
					d.memberStr(reduces.from()), d.pathStr(reduces.from())));
		}

		BoxReal inputBounds = ((HasBounds) value).getBounds();
		Prototype pt = ((HasPrototype) value).getPrototype();

		// Inherit dimensionality
		int inputDims = inputBounds.getRank();
		int outputDims = inputDims - reduces.reduceBy();
		Swizzle resizer = SwizzleFactory.resize(inputDims, outputDims);
		BoxReal bounds = new BoxReal(outputDims);
		resizer.swizzle(inputBounds, bounds);
		dimensions = Arrays.copyOfRange(pt.getDimensions(), reduces.reduceBy(),
				pt.getDimensions().length);
		log.debug("Dimensions of filter {} are \"{}\"", name, dimensions);
		log.debug("Bounds of filter {} inferred to be {}", name, bounds);
		setBounds(bounds);
	}

	void setBounds(BoxReal bounds) {
		this.bounds = bounds;
		internalCo = VectorReal.createEmpty(bounds.getRank());
		lastCo = VectorReal.createEmpty(bounds.getRank(), Double.NaN);
	}

	public String getName() {
		return name;
	}

	@Override
	public int getRank() {
		return bounds.getRank();
	}

	Class<?> getFieldType(String name) throws QueryConfigurationException {
		try {
			return innerFilter.getClass().getField(name).getType();
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
					"Could not access field \"%s\" of filter \"%s\"", name,
					this.name));
		}
	}

	/**
	 * Assign a uniform field by name.
	 *
	 * @param name The name of the field.
	 * @param value The value to set it to.
	 * @throws QueryConfigurationException If the value is the wrong type, if it
	 *         doesn't meet its constraints, or if the field can not be set to
	 *         the given value.
	 */
	// Package scope: allows Query to access.
	void setParameter(String name, Object value)
			throws QueryConfigurationException {

		Field f;

		try {
			f = innerFilter.getClass().getField(name);
		} catch (NoSuchFieldException e) {
			throw new QueryConfigurationException(String.format(
				"Parameter %s#%s is not defined.", innerFilter.getClass(),
				name), e);
		}

		if (value == null) {
			throw new QueryConfigurationException(String.format(
					"Parameter %s#%s should not be null.", innerFilter.getClass(),
					name));
		}

		Rank con = f.getAnnotation(Rank.class);
		checkConstraints(con, name, value);

		// Assign.
		try {
			f.set(innerFilter, value);
		} catch (IllegalAccessException e) {
			throw new QueryConfigurationException(String.format(
					"Parameter %s#%s can not be assigned.", innerFilter.getClass(),
					name), e);
		}
		log.info("Bound {} to {}", value,
				String.format("%s.%s", this.name, f.getName()));
	}

	void checkConstraints(Rank con, String name, Object value)
			throws QueryConfigurationException {

		if (con == null)
			return;

		if (con.is() >= 0) {
			if (!HasRank.class.isAssignableFrom(value.getClass()))
				throw new QueryConfigurationException(String.format(
						"Parameter %s.%s is has no dimensions; should be %dD.",
						getName(), name, con.is()));
			HasRank shaped = (HasRank) value;
			if (shaped.getRank() != con.is()) {
				throw new QueryConfigurationException(String.format(
						"Parameter %s.%s is %dD; should be %dD.",
						getName(), name, shaped.getRank(),
						con.is()));
			}
		}
	}

	/**
	 * @throws QueryConfigurationException
	 *             If any of the uniform or vaying fields are null or can't be
	 *             accessed.
	 */
	// Package scope: allows Query to access.
	void verifyConfiguration() throws QueryConfigurationException {

		for (Field f : innerFilter.getClass().getFields()) {
			if ((f.getModifiers() | Modifier.PUBLIC) == 0)
				continue;

			Object value;

			try {
				value = f.get(innerFilter);
			} catch (IllegalAccessException e) {
				throw new QueryConfigurationException(String.format(
						"Parameter \"%s\" is not accessible.", f.getName()), e);
			}

			if (value == null) {
				if (Cell.class.isAssignableFrom(f.getType())) {
					try {
						f.set(innerFilter, createCell(f.getName()));
					} catch (IllegalAccessException e) {
						throw new QueryConfigurationException(String.format(
								"Could not initialise unbound cell %s.",
								f.getName()));
					}
				} else {
					throw new QueryConfigurationException(String.format(
							"Public filter field %s.%s is unbound.",
							getName(), f.getName()));
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Filter(%s) {\n",
				innerFilter.getClass().getName()));

		for (Field f : innerFilter.getClass().getFields()) {
			if ((f.getModifiers() | Modifier.PUBLIC) == 0)
				continue;
			if (!PixelSource.class.isAssignableFrom(f.getType()) &&
					!Cell.class.isAssignableFrom(f.getType())) {
				continue;
			}
			try {
				sb.append(String.format("\t%s: %s\n", f.getName(),
						f.get(innerFilter)));
			} catch (Exception e) {
				continue;
			}
		}

		sb.append("}");
		return sb.toString();
	}

	@Override
	public void diagnostics() {
		if (!log.isInfoEnabled())
			return;

		try {
			for (Field f : innerFilter.getClass().getFields()) {
				Object ob = f.get(innerFilter);
				if (ob == null)
					continue;
				if (!Diagnostics.class.isAssignableFrom(ob.getClass()))
					continue;
				Diagnostics child = (Diagnostics) ob;
				child.diagnostics();
			}
		} catch (Exception e) {
			log.debug("Failed to list diagnostics information: {}",
					e.getMessage());
		}
	}

	public Filter getInnerFilter() {
		return innerFilter;
	}

	public void setInnerFilter(Filter innerFilter) {
		this.innerFilter = innerFilter;
	}

}
