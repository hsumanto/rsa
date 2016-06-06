/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.web.model.response;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Ledger;

@XmlRootElement(name = "Table")
public class TabularResponse {
	private String tableType;
	private String categorisation;
	private List<ArrayList<Double>> table;
	private List<TableColumn> columns;

	public TabularResponse() {
	}

	@XmlAttribute
	public String getCategorisation() {
		return categorisation;
	}
	public void setCategorisation(String categorisation) {
		this.categorisation = categorisation;
	}

	@XmlElementWrapper
	@XmlElement(name="row")
	public List<ArrayList<Double>> getRows() {
		return table;
	}
	public void setRows(List<ArrayList<Double>> table) {
		this.table = table;
	}

	@XmlAttribute
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	@XmlElement
	public List<TableColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}
}
