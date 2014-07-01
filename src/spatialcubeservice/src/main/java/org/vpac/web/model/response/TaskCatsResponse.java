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
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Stats;
import org.vpac.ndg.storage.model.TaskCats;

@XmlRootElement(name = "TaskCats")
public class TaskCatsResponse {
	private String id;
	private String taskId;
	private String name;
	private CellSize outputResolution;
	private TaskCats cat;
	private String tableType;
	private List<CatsElement> table;
	
	public String getId() {
		return id;
	}
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getTaskId() {
		return taskId;
	}
	@XmlAttribute
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
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
	public List<CatsElement> getRows() {
		return table;
	}
	@XmlAttribute
	public void setRows(List<CatsElement> table) {
		this.table = table;
	}
	
	public String getTableType() {
		return tableType;
	}
	@XmlAttribute
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}

	public TaskCatsResponse() {
	}
	
	public TaskCatsResponse(TaskCats cat) {
		this.setId(cat.getId());
		this.setTaskId(cat.getTaskId());
		this.setName(cat.getName());
		this.setOutputResolution(cat.getOutputResolution());
		this.cat = cat;
		this.setTableType("categories");
		outputResolution = cat.getOutputResolution();
	}
	
	public void processSummary(List<Double> lower, List<Double> upper) {
		CellSize outputResolution = this.outputResolution;
		List<CatsElement> result = new ArrayList<CatsElement>();
		double cellArea = outputResolution.toDouble() * outputResolution.toDouble();
		for (Entry<Integer, Hist> entry : this.cat.getCats().getCategories().entrySet()) {
			Stats s = new Stats();
			for (Bucket b : entry.getValue().getBuckets()) {
				if(filterBucket(b, lower, upper))
					s = s.fold(b.getStats());
			}
			if (s.getCount() > 0)
				result.add(new CatsElement(entry.getKey(), s.getCount() * cellArea));
		}
		this.setRows(result);
	}
	
	private boolean filterBucket(Bucket b, List<Double> lower, List<Double> upper) {
		if(lower.size() != upper.size()) throw new ArrayStoreException("lower & upper list doen't match");
		boolean inbound = false;
		for(int i = 0; i < lower.size(); i++) {
			if(b.getLower() > lower.get(i) && b.getUpper() < upper.get(i)) {
				inbound = true;
				break;
			}
		}
		return inbound;
	}
}
