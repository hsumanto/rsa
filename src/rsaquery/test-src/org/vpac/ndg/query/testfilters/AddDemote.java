package org.vpac.ndg.query.testfilters;

import java.io.IOException;

import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.filter.CellType;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.filter.InheritDimensions;
import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.VectorReal;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;

/**
 * This filter adds each pixel from two inputs together. If one input has more
 * dimensions than the other, it will be demoted.
 *
 * @author Alex Fraser
 */
@InheritDimensions(from = "in")
public class AddDemote implements Filter {

	@Rank(group="in", demote=true)
	public PixelSource inputA;

	@Rank(group="in", demote=true)
	public PixelSource inputB;

	@CellType("inputA")
	public Cell output;

	@Override
	public void initialise(BoxReal bounds) throws QueryConfigurationException {
	}

	@Override
	public void kernel(VectorReal coords) throws IOException {
		output.set(inputA.getPixel(coords).add(inputB.getPixel(coords)));
	}

}
