package org.vpac.web.model.response;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TableRow")
public class TableRowRaw {
	private List<Double> cells;

	public TableRowRaw() {
		cells = new ArrayList<>();
	}

	public TableRowRaw(List<Double> cells) {
		this.cells = cells;
	}

	public List<Double> getCells() {
		return cells;
	}

	@XmlElement
	public void setCells(List<Double> cells) {
		this.cells = cells;
	}
}
