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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.storage.model.TaskCats;

@XmlRootElement(name = "TaskCats")
public class TaskCatsResponse {
	private String id;
	private String taskId;
	private String name;
	private CellSize outputResolution;
	private Map<Integer, Double> catSummaries;
	
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
	public Map<Integer, Double> getCatSummaries() {
		return catSummaries;
	}
	@XmlAttribute
	public void setCatSummaries(Map<Integer, Double> catSummaries) {
		this.catSummaries = catSummaries;
	}
	
	public TaskCatsResponse() {
	}
	
	public TaskCatsResponse(List<TaskCats> cats) {
		TaskCats cat = cats.get(0);
		this.setId(cat.getId());
		this.setTaskId(cat.getTaskId());
		this.setCatSummaries(processSummary(cats));
	}
	
	private Map<Integer, Double> processSummary(List<TaskCats> cats) {
		Map<Integer, Double> result = new HashMap<Integer, Double>();
		return result;
	}
	
//	public TaskCats toTaskCats() {
//		TaskCats tc = new TaskCats(getTaskId(), getName(), getOutputResolution(),getCats());
//		return tc;
//	}
}
