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
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;

@XmlRootElement(name = "RangeTable")
public class RangeTableResponse {
	private String tableType;
	private String categorisation;
	private List<TableRowRanged> table;

	public RangeTableResponse() {
		this.setTableType("histogram");
	}

	public List<TableRowRanged> getRows() {
		return table;
	}
	@XmlAttribute
	public void setRows(List<TableRowRanged> table) {
		this.table = table;
	}
	public String getTableType() {
		return tableType;
	}
	@XmlAttribute
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public String getCategorisation() {
		return categorisation;
	}

	public void setCategorisation(String categorisation) {
		this.categorisation = categorisation;
	}

	public void setRows(Cats cats, CellSize resolution) {
		Hist summary = cats.summarise();
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<TableRowRanged> rows = new ArrayList<TableRowRanged>();
		for (Bucket b : summary.getBuckets()) {
			rows.add(new TableRowRanged(b.getLower(), b.getUpper(),
					b.getStats().getCount() * cellArea));
		}
		this.setRows(rows);
	}

}