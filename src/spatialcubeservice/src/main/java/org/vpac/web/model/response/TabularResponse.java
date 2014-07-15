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

	public TabularResponse() {
	}

	public String getCategorisation() {
		return categorisation;
	}
	@XmlAttribute
	public void setCategorisation(String categorisation) {
		this.categorisation = categorisation;
	}
	public List<T> getRows() {
		return table;
	}
	@XmlAttribute
	public void setRows(List<T> table) {
		this.table = table;
	}

	public String getTableType() {
		return tableType;
	}

	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	/**
	 * Data suitable for displaying as a bar chart. Each row has an ID.
	 */
	public static class TabularResponseCategorical extends TabularResponse<TableRow> {
		public TabularResponseCategorical() {
			setTableType("categories");
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

	/**
	 * Data suitable for displaying as a histogram. Each row has a lower and
	 * upper bound.
	 */
	public static class TabularResponseContinuous extends TabularResponse<TableRowRanged> {
		public TabularResponseContinuous() {
			setTableType("histogram");
		}

		public void setRows(Hist hist, CellSize resolution) {
			double cellArea = resolution.toDouble() * resolution.toDouble();
			List<TableRowRanged> rows = new ArrayList<TableRowRanged>();
			for (Bucket b : hist.getBuckets()) {
				rows.add(new TableRowRanged(b.getLower(), b.getUpper(),
						b.getStats().getCount() * cellArea));
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
		cats = cats.filterExtrinsic(categories);
		Hist hist = cats.summarise();

		if (categorical) {
			TabularResponseCategorical table = new TabularResponseCategorical();
			table.setRows(hist, resolution);
			return table;
		} else {
			TabularResponseContinuous table = new TabularResponseContinuous();
			table.setRows(hist, resolution);
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
		if (categorical)
			cats = cats.filterIntrinsic(values);
		else
			cats = cats.filterIntrinsic(lower, upper);
		cats = cats.optimise();

		TabularResponseCategorical table = new TabularResponseCategorical();
		table.setRows(cats, resolution);
		return table;
	}
}
