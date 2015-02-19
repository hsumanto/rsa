package org.vpac.ndg.query.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vpac.ndg.query.QueryException;

/**
 * Bases buckets on list of pre-defined values for bucket bounds
 *
 * <p>
 * A computed bucket bounds will simply return the upper and lower bounds
 * of the bucket the value falls into
 * </p>
 *
 * <p>
 * For example, the following is a list of bucket bounds (that would be passed to
 * setBuckets).  A value of 7.5 would return a bucket 0.0 - 10.0.
 * </p>
 *
 * <pre>
 * 0.0, 10.0, 20.0, 30.0, 40.0
 * </pre>
 *
 * @author lachlan, Alex Fraser
 */
public class BucketingStrategyExplicit implements BucketingStrategy, Serializable {

	private static final long serialVersionUID = 1L;

	private double[] buckets;

    public void setBounds(String bucketString) throws QueryException {
        String[] bucketValuesAsStrings = bucketString.split(",");
        List<Double> buckets = new ArrayList<Double>(bucketValuesAsStrings.length);

        for (String bvs : bucketValuesAsStrings) {
            bvs = bvs.trim();
            if (bvs.length() == 0)
                continue;
            double bucketValue = Double.parseDouble(bvs);
            buckets.add(bucketValue);
        }

        this.buckets = new double[buckets.size()];
        for (int i = 0; i < buckets.size(); i++) {
            this.buckets[i] = buckets.get(i);
        }
    }

    public double[] getBounds() {
        return this.buckets;
    }

    @Override
    public double[] computeBucketBounds(double value) {
        double secondLastBucketValue = Double.NEGATIVE_INFINITY;
        double lastBucketValue = Double.NEGATIVE_INFINITY;
        for (double bucketValue : this.buckets) {
            if (lastBucketValue <= value &&
                value < bucketValue) {
                return new double[] {lastBucketValue, bucketValue};
            }
            secondLastBucketValue = lastBucketValue;
            lastBucketValue = bucketValue;
        }

        // Special case whereby value is equal to the last buckets upper bound
        if (!Double.isInfinite(lastBucketValue) && value == lastBucketValue) {
            return new double[] {secondLastBucketValue, lastBucketValue};
        }

        return new double[] {lastBucketValue, Double.POSITIVE_INFINITY};
    }

    @Override
    public boolean isCategorical() {
        return false;
    }

    @Override
    public void checkConfiguration() throws QueryException {
        if (buckets == null) {
            throw new QueryException(
                "Explicit bucketing strategy: No buckets specified.");
        }
        for (int i = 1; i < buckets.length; i++) {
            if (buckets[i] <= buckets[i - 1]) {
                throw new QueryException(
                "Explicit bucketing strategy: Buckets must be stictly increase.");
            }
        }
    }
}
