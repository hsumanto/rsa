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
import org.vpac.ndg.storage.model.TaskCats;

@XmlRootElement(name = "TaskHist")
public class TaskHistResponse {
	private String id;
	private String taskId;
	private String name;
	private Cats cat;
	private CellSize outputResolution;
	private String tableType;
	private List<TableRowRanged> table;

	@XmlAttribute
	public String getId() {
		return id;
	}
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getTaskId() {
		return taskId;
	}
	@XmlAttribute
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	public CellSize getOutputResolution() {
		return outputResolution;
	}
	@XmlAttribute
	public void setOutputResolution(CellSize outputResolution) {
		this.outputResolution = outputResolution;
	}
	@XmlAttribute
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
	
	public TaskHistResponse() {
	}
	
	public TaskHistResponse(TaskCats cat) {
		this.setId(cat.getId());
		this.setTaskId(cat.getTaskId());
		this.setName("value");
		this.cat = cat.getCats();
		this.setTableType("histogram");
		outputResolution = cat.getOutputResolution();
	}
	
	public void processSummary(List<Integer> categories) {
		List<TableRowRanged> result = new ArrayList<TableRowRanged>();
		Hist histSummary = new Hist();
		for (Integer key : cat.getCategories().keySet()) {
			if (categories == null)
				histSummary = histSummary.fold(cat.getCategories().get(key));
			else if (categories.contains(key))
				histSummary = histSummary.fold(cat.getCategories().get(key));
		}
		CellSize outputResolution = this.outputResolution;
		double cellArea = outputResolution.toDouble() * outputResolution.toDouble();
		for (Bucket b : histSummary.getBuckets()) {
			if (b.getStats().getCount() > 0)
				result.add(new TableRowRanged(b.getLower(), b.getUpper(), b.getStats().getCount() * cellArea));
		}
		this.setRows(result);
	}
}