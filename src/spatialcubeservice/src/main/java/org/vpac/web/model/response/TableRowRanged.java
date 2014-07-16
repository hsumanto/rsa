package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TableRowRanged")
public class TableRowRanged {
	private double lower;
	private double upper;
	private double area;
	private Double fraction;

	public TableRowRanged() {
	}

	public TableRowRanged(double lower, double upper, double area) {
		this.lower = lower;
		this.upper = upper;
		this.area = area;
	}

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

	public double getArea() {
		return area;
	}

	@XmlAttribute
	public void setArea(double area) {
		this.area = area;
	}

	public Double getFraction() {
		return fraction;
	}

	@XmlAttribute
	public void setFraction(Double fraction) {
		this.fraction = fraction;
	}
}
