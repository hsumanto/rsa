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
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.DatasetCats;

@XmlRootElement(name = "TaskHist")
public class DatasetHistResponse {
	private String id;
	private String datasetId;
	private String timeSliceId;
	private String bandId;
	private String tableType;
	private Cats cat;
	private List<HistElement> table;
	private Dataset dataset;

	public String getDatasetId() {
		return datasetId;
	}
	@XmlAttribute
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public String getTimeSliceId() {
		return timeSliceId;
	}
	@XmlAttribute
	public void setTimeSliceId(String timeSliceId) {
		this.timeSliceId = timeSliceId;
	}
	public String getBandId() {
		return bandId;
	}
	@XmlAttribute
	public void setBandId(String bandId) {
		this.bandId = bandId;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public List<HistElement> getRows() {
		return table;
	}
	@XmlAttribute
	public void setRows(List<HistElement> table) {
		this.table = table;
	}
	public String getTableType() {
		return tableType;
	}
	@XmlAttribute
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	
	public DatasetHistResponse() {
	}
	
	public DatasetHistResponse(DatasetCats cat, Dataset ds) {
		this.setId(cat.getId());
		this.setDatasetId(cat.getDatasetId());
		this.setTimeSliceId(cat.getTimeSliceId());
		this.setBandId(cat.getBandId());
		this.setTableType("histogram");
		this.cat = cat.getCats();
		this.dataset = ds;
	}
	
	public void processSummary(List<Integer> categories) {
		List<HistElement> result = new ArrayList<HistElement>();
		Hist histSummary = new Hist();
		for (Integer key : cat.getCategories().keySet()) {
			if (categories == null)
				histSummary = histSummary.fold(cat.getCategories().get(key));
			else if (categories.contains(key))
				histSummary = histSummary.fold(cat.getCategories().get(key));
		}
		CellSize outputResolution = dataset.getResolution();
		double cellArea = outputResolution.toDouble() * outputResolution.toDouble();
		for (Bucket b : histSummary.getBuckets()) {
			if (b.getStats().getCount() > 0)
				result.add(new HistElement(b.getLower(), b.getUpper(), b.getStats().getCount() * cellArea));
		}
		this.setRows(result);
	}
}