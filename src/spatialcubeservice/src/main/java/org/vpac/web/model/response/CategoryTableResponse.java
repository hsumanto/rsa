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
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Stats;

@XmlRootElement(name = "CategoryTable")
public class CategoryTableResponse {
	private String tableType;
	private String categorisation;
	private List<TableRow> table;

	public CategoryTableResponse() {
		this.setTableType("categories");
	}

	public String getCategorisation() {
		return categorisation;
	}
	@XmlAttribute
	public void setCategorisation(String categorisation) {
		this.categorisation = categorisation;
	}
	public List<TableRow> getRows() {
		return table;
	}
	@XmlAttribute
	public void setRows(List<TableRow> table) {
		this.table = table;
	}

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public void setRows(Cats cats, CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<TableRow> rows = new ArrayList<TableRow>();
		for (Entry<Integer, Hist> entry : cats.getCategories().entrySet()) {
			Hist hist = entry.getValue();
			Stats s = hist.summarise();
			rows.add(new TableRow(entry.getKey(), s.getCount() * cellArea));
		}
		setRows(rows);
	}

	public void setRows(Hist hist, CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<TableRow> rows = new ArrayList<TableRow>();
		for (Bucket b : hist.getBuckets()) {
			rows.add(new TableRow(b.getLower(),
					b.getStats().getCount() * cellArea));
		}
		this.setRows(rows);
	}
}
