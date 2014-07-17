package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TableRow")
public class TableRow {
	private double id;
	private double area;
	private Double rawArea;

	public TableRow() {
	}

	public TableRow(double id, double area) {
		this.id = id;
		this.area = area;
	}

	public double getId() {
		return id;
	}

	@XmlAttribute
	public void setId(double id) {
		this.id = id;
	}

	public double getArea() {
		return area;
	}

	@XmlAttribute
	public void setArea(double area) {
		this.area = area;
	}

	public Double getRawArea() {
		return rawArea;
	}

	public void setRawArea(Double rawArea) {
		this.rawArea = rawArea;
	}
}
