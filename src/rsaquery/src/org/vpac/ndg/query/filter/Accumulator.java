package org.vpac.ndg.query.filter;

public interface Accumulator<F extends Foldable<F>> {

	F getAccumulatedOutput();

}
