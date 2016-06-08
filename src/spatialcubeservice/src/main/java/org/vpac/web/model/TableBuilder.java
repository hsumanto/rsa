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

package org.vpac.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.web.model.response.TableColumn;
import org.vpac.web.model.response.TabularResponse;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.query.stats.Stats;

/**
 * Unstructured data.
 */
public class TableBuilder {

	/**
	 * @return A table of data, with each row representing a bucket in the
	 * histograms of the Cats object.
	 */
	public TabularResponse buildIntrinsic(Cats cats,
			List<Integer> categories, CellSize resolution,
			boolean categorical) {
		Cats filteredCats = cats.filterExtrinsic(categories);
		Hist filteredHist = filteredCats.summarise();
		Hist unfilteredHist = cats.summarise();

		TabularResponse table = new TabularResponse();
		if (categorical) {
			table.setTableType("categories");
			table.setColumns(categoricalColumns());
			table.setRows(categoricalRows(
				filteredHist, unfilteredHist, resolution));
		} else {
			table.setTableType("histogram");
			table.setColumns(continuousColumns());
			table.setRows(continuousRows(
				filteredHist, unfilteredHist, resolution));

			// Set min and max of value column. These can't be determined from
			// the data in the rows because it is grouped into buckets.
			Stats s = unfilteredHist.summarise();
			table.getColumns().get(0)
				.min(s.getMin())
				.max(s.getMax());
		}
		return table;
	}

	/**
	 * @return A table of data, with each row representing a category of the
	 * provided Cats object.
	 */
	public TabularResponse buildExtrinsic(Cats cats,
			List<Double> lower, List<Double> upper, List<Double> values,
			CellSize resolution, boolean categorical) {
		Cats filteredCats;
		if (categorical)
			filteredCats = cats.filterIntrinsic(values);
		else
			filteredCats = cats.filterIntrinsic(lower, upper);

		TabularResponse table = new TabularResponse();
		table.setTableType("categories");
		table.setColumns(categoricalColumns());
		table.setRows(categoricalRows(filteredCats, cats, resolution));
		return table;
	}

	public TabularResponse buildLedger(Ledger ledger,
			List<Integer> columns, CellSize resolution) {
		// Filtering columns does not result in a "filtered" ledger. Only a
		// ledger with a different volume (i.e. data *removed* due to
		// filtered rows) would be considered filtered.
		if (columns != null && columns.size() > 0)
			ledger = ledger.filter(columns);
		TabularResponse table = new TabularResponse();
		table.setTableType("ledger");
		table.setColumns(ledgerColumns(ledger));
		table.setRows(ledgerRows(ledger, ledger, resolution));
		return table;
	}

	private List<TableColumn> categoricalColumns() {
		List<TableColumn> columns = new ArrayList<TableColumn>();
		TableColumn category = new TableColumn()
			.key(0).name("Category").type("category")
			.description("The category of the data.");
		TableColumn area = new TableColumn()
			.key(1).name("Area").units("m^2").type("area")
			.description("The area of land that matches the filters.");
		TableColumn rawArea = new TableColumn()
			.key(2).name("Unfiltered Area").units("m^2").type("area")
			.description("The area of available land.");
		area.portionOf(rawArea.getKey());
		columns.add(category);
		columns.add(area);
		columns.add(rawArea);
		return columns;
	}

	public List<ArrayList<Double>> categoricalRows(Cats cats, Cats unfilteredCats,
			CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<ArrayList<Double>> rows = new ArrayList<>();
		for (Entry<Integer, Hist> entry : cats.getCategories().entrySet()) {
			ArrayList<Double> row = new ArrayList<>();
			row.add(entry.getKey().doubleValue());

			Hist hist = entry.getValue();
			Stats s = hist.summarise();
			row.add(s.getCount() * cellArea);

			Hist unfilteredHist = unfilteredCats.get(entry.getKey());
			if (unfilteredHist != null) {
				s = unfilteredHist.summarise();
				row.add(s.getCount() * cellArea);
			}

			rows.add(row);
		}
		return rows;
	}

	public List<ArrayList<Double>> categoricalRows(Hist hist, Hist unfilteredHist,
			CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<ArrayList<Double>> rows = new ArrayList<>();
		for (Bucket b : hist.getBuckets()) {
			ArrayList<Double> row = new ArrayList<>();
			row.add(b.getLower());

			Stats s = b.getStats();
			row.add(s.getCount() * cellArea);

			Bucket ub = unfilteredHist.getBucket(b.getLower());
			if (ub != null) {
				s = ub.getStats();
				row.add(s.getCount() * cellArea);
			}

			rows.add(row);
		}
		return rows;
	}

	private List<TableColumn> continuousColumns() {
		List<TableColumn> columns = new ArrayList<TableColumn>();
		TableColumn lower = new TableColumn()
			.key(0).name("Lower Bound").type("lowerBound")
			.description("The lower bound of the grouping (value range).");
		TableColumn upper = new TableColumn()
			.key(1).name("Upper Bound").type("upperBound")
			.description("The upper bound of the grouping (value range).");
		TableColumn area = new TableColumn()
			.key(2).name("Area").units("m^2").type("area")
			.description("The area of land that matches the filters.");
		TableColumn rawArea = new TableColumn()
			.key(3).name("Unfiltered Area").units("m^2").type("area")
			.description("The area of available land.");
		area.portionOf(rawArea.getKey());
		columns.add(lower);
		columns.add(upper);
		columns.add(area);
		columns.add(rawArea);
		return columns;
	}

	public List<ArrayList<Double>> continuousRows(Hist hist, Hist unfilteredHist,
			CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<ArrayList<Double>> rows = new ArrayList<>();
		for (Bucket b : hist.getBuckets()) {
			ArrayList<Double> row = new ArrayList<>();
			row.add(b.getLower());
			row.add(b.getUpper());

			Stats s = b.getStats();
			row.add(s.getCount() * cellArea);

			Bucket ub = unfilteredHist.getBucket(b.getLower());
			if (ub != null) {
				s = ub.getStats();
				row.add(s.getCount() * cellArea);
			}

			rows.add(row);
		}
		return rows;
	}

	public List<TableColumn> ledgerColumns(Ledger ledger) {
		List<TableColumn> columns = new ArrayList<TableColumn>();
		int i = 0;
		for (String bs : ledger.getBucketingStrategies()) {
			if (bs.equals("categorical")) {
				columns.add(new TableColumn()
					.key(i++).name("Category").type("category")
					.description("The category of the data."));
			} else {
				columns.add(new TableColumn()
					.key(i++).name("Lower Bound").type("lowerBound")
					.description("The lower bound of the grouping (value range)."));
			}
		}
		TableColumn area = new TableColumn()
			.key(i++).name("Area").units("m^2").type("area")
			.portionOf(i+1)
			.description("The area of land that matches the filters.");
		TableColumn rawArea = new TableColumn()
			.key(i++).name("Unfiltered Area").units("m^2").type("area")
			.description("The area of available land.");
		area.portionOf(rawArea.getKey());
		columns.add(area);
		columns.add(rawArea);
		return columns;
	}

	public List<ArrayList<Double>> ledgerRows(Ledger ledger,
			Ledger unfilteredLedger, CellSize resolution) {
		double cellArea = resolution.toDouble() * resolution.toDouble();
		List<ArrayList<Double>> rows = new ArrayList<>();
		for (Map.Entry<List<Double>, Long> entry : ledger.entrySet()) {
			ArrayList<Double> cells = new ArrayList<>();
			cells.addAll(entry.getKey());
			cells.add(entry.getValue() * cellArea);
			cells.add(unfilteredLedger.get(entry.getKey()) * cellArea);
			rows.add(new ArrayList<>(cells));
		}
		return rows;

		// Should set min and max on the input columns too; need to get this
		// info from unfilteredLedger.
	}
}
