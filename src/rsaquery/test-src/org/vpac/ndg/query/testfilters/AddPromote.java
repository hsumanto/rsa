package org.vpac.ndg.query.testfilters;

import java.io.IOException;

import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;

/**
 * This filter adds each pixel from two inputs together. If one input has fewer
 * dimensions than the other, it will be promoted.
 *
 * @author Alex Fraser
 */
@InheritDimensions(from = "in")
public class AddPromote implements Filter {

	@Rank(group="in", promote=true)
	public PixelSource inputA;

	@Rank(group="in", promote=true)
	public PixelSource inputB;

	@CellType("inputA")
	public Cell output;

	@Override
	public void initialise(BoxReal bounds) throws QueryException {
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		output.set(inputA.getPixel(coords).add(inputB.getPixel(coords)));
	}

}
