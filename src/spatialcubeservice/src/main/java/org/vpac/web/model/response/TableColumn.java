package org.vpac.web.model.response;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Column")
public class TableColumn {
	private String key;
	private String name;
	private String description;
	private String units;
	private String type;
	private String portionOf;

	public TableColumn() {
	}

	@XmlAttribute
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public TableColumn key(String key) {
		this.key = key;
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
	public String getPortionOf() {
		return portionOf;
	}
	public void setPortionOf(String portionOf) {
		this.portionOf = portionOf;
	}
	public TableColumn portionOf(String portionOf) {
		this.portionOf = portionOf;
		return this;
	}
}
