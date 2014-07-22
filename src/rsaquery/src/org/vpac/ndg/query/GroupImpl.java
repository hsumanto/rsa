package org.vpac.ndg.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.coordinates.HasBounds;
import org.vpac.ndg.query.coordinates.HasRank;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Swizzle;
import org.vpac.ndg.query.math.SwizzleFactory;
import org.vpac.ndg.query.sampling.HasDimensions;
import org.vpac.ndg.query.sampling.PixelSource;
import org.vpac.ndg.query.sampling.PixelSourceScalar;
import org.vpac.ndg.query.sampling.PixelSourceVector;
import org.vpac.ndg.query.sampling.SwizzledPixelScalar;
import org.vpac.ndg.query.sampling.SwizzledPixelVector;

/**
 * Helper class for processing grouped constraints (where fields are
 * co-dependent).
 *
 * @author Alex Fraser
 */
public class GroupImpl implements HasRank, HasDimensions, HasBounds {
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
					"Could not access field %s.",
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

		if (!rank.promote()) {
			if (source.getRank() < rankLower) {
				throw new QueryConfigurationException(String.format(
						"Field %s can't have fewer dimensions than %s. Path is %s.",
						d.memberStr(field), d.memberStr(rankLowerField),
						d.pathStr(field)));
			}
			if (source.getRank() < rankUpper) {
				rankUpper = source.getRank();
				rankUpperField = field;
			}
		}
		if (!rank.demote()) {
			if (source.getRank() > rankUpper) {
				throw new QueryConfigurationException(String.format(
						"Field %s can't have more dimensions than %s. Path is"
						+ " %s",
						d.memberStr(field), d.memberStr(rankUpperField),
						d.pathStr(field)));
			}
			if (source.getRank() > rankLower) {
				rankLower = source.getRank();
				rankLowerField = field;
			}
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

		// Reorder dimensions first.
		dimensions = null;
		Field dimsField = null;
		for (Field field : members) {
			PixelSource source = getValue(field);
			Swizzle swizzle = SwizzleFactory.resize(source.getRank(),
					targetRank);
			if (source.getRank() >= targetRank) {
				// Use this source for the dimension names.
				String[] dims = new String[targetRank];
				swizzle.swizzle(source.getPrototype().getDimensions(), dims);
				if (dimensions == null) {
					dimensions = dims;
					dimsField = field;
				} else if (!Arrays.equals(dimensions, dims)) {
					throw new QueryConfigurationException(String.format(
							"Could not determine dimensions of group %s:"
							+ " can't decide between candidates %s"
							+ " specified by %s and %s specified by %s.",
							this.name, dimensions, dimsField,
							dims, field));
				}
			}
		}

		// Adjust rank, and wrap
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

			Swizzle swizzle = SwizzleFactory.resize(source.getRank(),
					targetRank);
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

		// Union bounds of all members. Note that any virtual dimensions (due
		// to promotion) will be ignored, because they will have zero length.
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
