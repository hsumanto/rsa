package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Column")
public class TableColumn {
	private int key;
	private Integer inputIndex;
	private String name;
	private String description;
	private String units;
	private String type;
	private Integer portionOf;
	private Double min;
	private Double max;

	public TableColumn() {
	}

	@XmlAttribute
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public TableColumn key(int key) {
		this.key = key;
		return this;
	}

	@XmlAttribute
	public Integer getInputIndex() {
		return inputIndex;
	}
	public void setInputIndex(Integer inputIndex) {
		this.inputIndex = inputIndex;
	}
	public TableColumn inputIndex(Integer inputIndex) {
		this.inputIndex = inputIndex;
		return this;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TableColumn name(String name) {
		this.name = name;
		return this;
	}

	@XmlAttribute
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public TableColumn description(String description) {
		this.description = description;
		return this;
	}

	@XmlAttribute
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public TableColumn units(String units) {
		this.units = units;
		return this;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TableColumn type(String type) {
		this.type = type;
		return this;
	}

	@XmlAttribute
	public Integer getPortionOf() {
		return portionOf;
	}
	public void setPortionOf(Integer portionOf) {
		this.portionOf = portionOf;
	}
	public TableColumn portionOf(Integer portionOf) {
		this.portionOf = portionOf;
		return this;
	}

	@XmlAttribute
	public Double getMin() {
		return min;
	}
	public void setMin(Double min) {
		this.min = min;
	}
	public TableColumn min(Double min) {
		this.min = min;
		return this;
	}

	@XmlAttribute
	public Double getMax() {
		return max;
	}
	public void setMax(Double max) {
		this.max = max;
	}
	public TableColumn max(Double max) {
		this.max = max;
		return this;
	}

}
