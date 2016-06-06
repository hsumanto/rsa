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

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Ledger;

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

	@XmlElement
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

	@XmlElement
	public List<TableColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
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

	public static TabularResponse<?> tabulateLedger(Ledger ledger,
			List<Integer> columns, CellSize resolution) {
		// Filtering columns does not result in a "filtered" ledger. Only a
		// ledger with a different volume (i.e. data *removed* due to
		// filtered rows) would be considered filtered.
		if (columns != null && columns.size() > 0)
			ledger = ledger.filter(columns);
		TabularResponseLedger table = new TabularResponseLedger();
		table.setData(ledger, ledger, resolution);
		return table;
	}
}
