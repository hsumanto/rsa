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

package org.vpac.ndg.query.filter;

import java.io.IOException;

import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.Element;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.CellScalar;
import org.vpac.ndg.query.sampling.CellVector;
import org.vpac.ndg.query.sampling.PixelSource;
import org.vpac.ndg.query.sampling.PixelSourceScalar;
import org.vpac.ndg.query.sampling.PixelSourceVector;

/**
 * Encapsulates a function to run over pixels in an image.
 *
 * <p>
 * The framework will create a pool of instances of the filter, which will be
 * used to iterate over the data in a stream processing fashion.
 * </p>
 *
 * <p>
 * <img src="doc-files/Filter_class.png" />
 * </p>
 *
 * <h3>Binding</h3>
 *
 * <p>
 * The query engine binds inputs and outputs by name, according to a
 * user-supplied {@link QueryDefinition}. All <em>public</em> filter fields
 * will be made available to be bound as an input or output. Any public fields
 * that have not been bound will result in an error.
 * </p>
 *
 * <p>
 * Inputs are bound to {@link PixelSource} fields. You may use
 * {@link PixelSourceScalar} or {@link PixelSourceVector} if your filter
 * requires one of those types in particular.
 * </p>
 *
 * <p>
 * Outputs are bound to {@link Cell} fields. You may use {@link CellScalar} or
 * {@link CellVector} if your filter outputs one of those types in particular.
 * </p>
 *
 * <p>
 * Literal (constant) values can be bound to fields of class {@link String} and
 * numbers (double, int, etc.)
 * </p>
 *
 * <h3>Dimensionality</h3>
 *
 * <p>
 * All outputs ({@link Cell Cells}) of a filter must have the same shape
 * (number and length of dimensions). The shape is inherited from one of the
 * inputs. Filter classes must be annotated with {@link InheritDimensions}.
 * </p>
 *
 * <p>
 * Constraints may be placed on the dimensionality of the inputs using the
 * {@link Rank} annotation. In the following example, <em>inputA</em> and
 * <em>inputB</em> must have the same dimensionality, but <em>inputB</em> will
 * be automatically promoted if it has too few dimensions.
 * </p>
 *
 * <pre>
 * <code>
 * public PixelSource inputA;
 *
 * {@literal @}Rank(promote = true, group = "inputA")
 * public PixelSource inputB;
 * </code>
 * </pre>
 *
 * <h3>Inheritance</h3>
 *
 * <p>
 * Because queries are configured and bound using public fields, care must be
 * taken when extending filter classes. Never use the same field name in a
 * subclass, because it will hide the field in the parent class and result in
 * binding errors (the query will refuse to run).
 * </p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Stream_processing">Stream
 *      processing</a>
 * @see PixelSource Input fields
 * @see Cell Output fields
 *
 * @author Alex Fraser
 */
public interface Filter {
	/**
	 * Called immediately before the kernel function runs for the first time.
	 * The values of fields will not be changed by the framework after this
	 * function runs; this is not true for the constructor. Should be used to
	 * configure the filter, e.g. set up custom views of the data and initialise
	 * local storage (such as {@link Element} instances).
	 *
	 * @param bounds The extents that this filter will operate over, in global
	 * cell space.
	 */
	void initialise(BoxReal bounds) throws QueryException;

	/**
	 * Process a single pixel. This is the heart of a filter. This method
	 * should take the given coordinates and write values to the filter's
	 * {@link Cell output fields}. Optionally, data may be read from
	 * {@link PixelSource input fields}.
	 *
	 * @param outputCoords The coordinates to generate a value for, in pixel
	 * space. E.g. the first pixel ranges across coordinages 0.0 - 1.0 in all
	 * dimensions. If this filter is being used to populate an output variable
	 * (i.e. if the user has chosen to connect it to a file on disk), the query
	 * engine will ask for values centred on each pixel (i.e. with an offset
	 * 0.5). When called by another filter, some other offset may be given.
	 * @throws IOException If the inputs can not be read from.
	 */
	void kernel(VectorReal outputCoords) throws IOException;
}
