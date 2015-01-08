package org.vpac.ndg.query.stats;

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
 * @author lachlan
 */
public class BucketingStrategyExplicit implements BucketingStrategy {

    private double[] buckets;

    public void setBounds(String bucketString) {
        String[] bucketValuesAsStrings = bucketString.split(",");
        double[] buckets = new double[bucketValuesAsStrings.length];

        for (int i = 0; i < bucketValuesAsStrings.length; i++) {
            double bucketValue = Double.parseDouble(bucketValuesAsStrings[i].trim());
            buckets[i] = bucketValue;
        }

        this.buckets = buckets;
    }

    public double[] getBounds() {
        return this.buckets;
    }

    @Override
    public double[] computeBucketBounds(double value) {

        double lastBucketValue = Double.NaN;
        for (double bucketValue : this.buckets) {
            if (lastBucketValue != Double.NaN && 
                lastBucketValue <= value &&
                value < bucketValue) {
                return new double[] {lastBucketValue, bucketValue};
            }
            lastBucketValue = bucketValue;
        }

        // Or throw exception?
        return null;
    }

    @Override
    public boolean isCategorical() {
        return false;
    }

}
