package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HistElement")
public class HistElement {
	private double lower;
	private double upper;
	private long count;
	
	public double getLower() {
		return lower;
	}
	@XmlAttribute
	public void setLower(double lower) {
		this.lower = lower;
	}
	public double getUpper() {
		return upper;
	}
	@XmlAttribute
	public void setUpper(double upper) {
		this.upper = upper;
	}
	public long getCount() {
		return count;
	}
	@XmlAttribute
	public void setCount(long count) {
		this.count = count;
	}
	public long getArea() {
        return count;
    }
    @XmlAttribute
    public void setArea(long count) {
        this.count = count;
    }
	
	public HistElement() {
	}

	public HistElement(double lower, double upper, long count) {
		this.lower = lower;
		this.upper = upper;
		this.count = count;
	}
}
