package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TableRow")
public class TableRow {
	private Integer id;
	private double area;

	public Integer getId() {
		return id;
	}

	@XmlAttribute
	public void setId(Integer id) {
		this.id = id;
	}

	public double getArea() {
		return area;
	}

	@XmlAttribute
	public void setArea(double area) {
		this.area = area;
	}

	public TableRow() {
	}

	public TableRow(Integer id, double area) {
		this.id = id;
		this.area = area;
	}
}
