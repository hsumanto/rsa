package org.vpac.ndg.query.stats;

import java.io.Serializable;

import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.math.ScalarElement;

/**
 * A bucket in a histogram.
 * @author Alex Fraser
 */
public class Bucket implements Foldable<Bucket>, Serializable {

	private static final long serialVersionUID = 1L;
	private String id;	
	private double lower;
	private double upper;
	private Stats stats;

	private static final double EPSILON = 1e-9;

	public Bucket() {
	}

	public Bucket(Double lower, Double upper, Stats stats) {
		this.lower = lower;
		this.upper = upper;
		this.stats = stats;
	}

	public Bucket copy() {
		Bucket res = new Bucket();
		res.lower = this.lower;
		res.upper = this.upper;
		res.stats = this.stats.copy();
		return res;
	}

	public boolean canContain(ScalarElement value) {
		return canContain(value.doubleValue());
	}

	public boolean canContain(double value) {
		if (lower == upper) {
			// Special case for categorical (zero-range) buckets.
			if (Double.isInfinite(value))
				return value == lower;
			else if (value < lower - lower * EPSILON)
				return false;
			else if (value > upper + upper * EPSILON)
				return false;
			else
				return true;
		} else if (value < lower)
			return false;
		else if (value >= upper)
			return false;
		else
			return true;
	}

	public boolean intersects(Bucket other) {
		if (lower == upper)
			// Special case for categorical (zero-range) buckets.
			return other.canContain(lower);
		else if (other.lower == other.upper)
			// Special case for categorical (zero-range) buckets.
			return canContain(other.lower);
		else if (other.lower >= upper)
			return false;
		else if (other.upper <= lower)
			return false;
		else
			return true;
	}

	@Override
	public Bucket fold(Bucket other) {
		return new Bucket(Math.min(lower, other.lower),
				Math.max(upper, other.upper), stats.fold(other.stats));
	}

	public double getLower() {
		return lower;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}

	public double getUpper() {
		return upper;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	

	@Override
	public String toString() {
		return String.format("Bucket(%g-%g)", lower, upper);
	}

}
