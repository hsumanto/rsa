package org.vpac.ndg.query.stats;



public class BucketingStrategyFalseLog extends BucketingStrategyLog {

	private static final long serialVersionUID = 1L;

	@Override
	double lowerBound(int i) {
		// Lower bound = n^(floor(i/nb)+logn(i%nb) + 1)
		double wholePart = Math.floor(i / root);
		double fraction;
		if (i % root < EPSILON)
			fraction = 0;
		else
			fraction = logN(base, (i % root) / root) + 1;
		double lower = Math.pow(base, wholePart + fraction);
		lower *= scale;
		return lower;
	}

	@Override
	int indexOf(double value) {
		double logarithm = logN(base, value / scale) + EPSILON;
		double wholePart = Math.floor(logarithm) * root;
		double fraction = Math.floor((root * Math.pow(base, logarithm % 1.0)) / base);
		double index = wholePart + fraction;
		index += EPSILON;
		return (int) index;
	}
}
