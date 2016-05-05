package org.vpac.ndg.query.testfilters;

import org.vpac.ndg.query.filter.Rank;
import org.vpac.ndg.query.sampling.PixelSourceScalar;

/**
 * A filter that can not run because it hides a field of its parent.
 * @author Alex Fraser
 */
public class BrokenInheritanceFilter extends ActiveFire {
	@Rank(is = 3)
	public PixelSourceScalar input;
}
