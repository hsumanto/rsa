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

@XmlRootElement(name = "Table")
public class TabularResponse <T> {
	private String tableType;
	private String categorisation;
	private List<T> table;
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

	@XmlAttribute
	public List<T> getRows() {
		return table;
	}
	public void setRows(List<T> table) {
		this.table = table;
	}

	@XmlAttribute
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	@XmlAttribute
	public List<TableColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}

	/**
	 * Data suitable for displaying as a bar chart. Each row has an ID.
	 */
	public static class TabularResponseCategorical extends TabularResponse<TableRow> {
		public TabularResponseCategorical() {
			setTableType("categories");

			List<TableColumn> columns = new ArrayList<TableColumn>();
			columns.add(new TableColumn()
					.key("id").name("Category").type("category"));
			columns.add(new TableColumn()
					.key("area").name("Area").units("m^2").type("area")
					.portionOf("rawArea"));
			columns.add(new TableColumn().key("rawArea")
					.name("Unfiltered Area").units("m^2").type("area"));
			setColumns(columns);
		}

		public void setRows(Cats cats, Cats unfilteredCats, CellSize resolution) {
			double cellArea = resolution.toDouble() * resolution.toDouble();
			List<TableRow> rows = new ArrayList<TableRow>();
			for (Entry<Integer, Hist> entry : cats.getCategories().entrySet()) {
				TableRow row = new TableRow();
				row.setId(entry.getKey());

				Hist hist = entry.getValue();
				Stats s = hist.summarise();
				row.setArea(s.getCount() * cellArea);

				Hist unfilteredHist = unfilteredCats.get(entry.getKey());
				if (unfilteredHist != null) {
					s = unfilteredHist.summarise();
					row.setRawArea(s.getCount() * cellArea);
				}

				rows.add(row);
			}
			setRows(rows);
		}

		public void setRows(Hist hist, Hist unfilteredHist, CellSize resolution) {
			double cellArea = resolution.toDouble() * resolution.toDouble();
			List<TableRow> rows = new ArrayList<TableRow>();
			for (Bucket b : hist.getBuckets()) {
				TableRow row = new TableRow();
				row.setId(b.getLower());

				Stats s = b.getStats();
				row.setArea(s.getCount() * cellArea);

				Bucket ub = unfilteredHist.getBucket(b.getLower());
				if (ub != null) {
					s = ub.getStats();
					row.setRawArea(s.getCount() * cellArea);
				}

				rows.add(row);
			}
			setRows(rows);
		}
	}

	/**
	 * Data suitable for displaying as a histogram. Each row has a lower and
	 * upper bound.
	 */
	public static class TabularResponseContinuous extends TabularResponse<TableRowRanged> {
		public TabularResponseContinuous() {
			setTableType("histogram");

			List<TableColumn> columns = new ArrayList<TableColumn>();
			columns.add(new TableColumn()
					.key("lower").name("Lower Bound").type("length"));
			columns.add(new TableColumn()
					.key("upper").name("Upper Bound").type("length"));
			columns.add(new TableColumn()
					.key("area").name("Area").units("m^2").type("area")
					.portionOf("rawArea"));
			columns.add(new TableColumn()
					.key("rawArea").name("Unfiltered Area").units("m^2")
					.type("area"));
			setColumns(columns);
		}

		public void setRows(Hist hist, Hist unfilteredHist, CellSize resolution) {
			double cellArea = resolution.toDouble() * resolution.toDouble();
			List<TableRowRanged> rows = new ArrayList<TableRowRanged>();
			for (Bucket b : hist.getBuckets()) {
				TableRowRanged row = new TableRowRanged();
				row.setLower(b.getLower());
				row.setUpper(b.getUpper());

				Stats s = b.getStats();
				row.setArea(s.getCount() * cellArea);

				Bucket ub = unfilteredHist.getBucket(b.getLower());
				if (ub != null) {
					s = ub.getStats();
					row.setRawArea(s.getCount() * cellArea);
				}

				rows.add(row);
			}
			this.setRows(rows);
		}
	}

	/**
	 * @return A table of data, with each row representing a bucket in the
	 * histograms of the Cats object.
	 */
	public static TabularResponse<?> tabulateIntrinsic(Cats cats,
			List<Integer> categories, CellSize resolution, boolean categorical) {
		Cats filteredCats = cats.filterExtrinsic(categories);
		Hist filteredHist = filteredCats.summarise();
		Hist unfilteredHist = cats.summarise();

		if (categorical) {
			TabularResponseCategorical table = new TabularResponseCategorical();
			table.setRows(filteredHist, unfilteredHist, resolution);
			return table;
		} else {
			TabularResponseContinuous table = new TabularResponseContinuous();
			table.setRows(filteredHist, unfilteredHist, resolution);
			return table;
		}
	}

	/**
	 * @return A table of data, with each row representing a category of the
	 * provided Cats object.
	 */
	public static TabularResponse<?> tabulateExtrinsic(Cats cats,
			List<Double> lower, List<Double> upper, List<Double> values,
			CellSize resolution, boolean categorical) {
		Cats filteredCats;
		if (categorical)
			filteredCats = cats.filterIntrinsic(values);
		else
			filteredCats = cats.filterIntrinsic(lower, upper);

		TabularResponseCategorical table = new TabularResponseCategorical();
		table.setRows(filteredCats, cats, resolution);
		return table;
	}
}
