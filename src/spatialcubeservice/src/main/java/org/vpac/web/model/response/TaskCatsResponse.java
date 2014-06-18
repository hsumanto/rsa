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
import java.util.Map;
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
	private Map<Integer, Long> catSummaries;
	
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
	public Map<Integer, Long> getCatSummaries() {
		return catSummaries;
	}
	@XmlAttribute
	public void setCatSummaries(Map<Integer, Long> catSummaries) {
		this.catSummaries = catSummaries;
	}
	
	public TaskCatsResponse() {
	}
	
	public TaskCatsResponse(TaskCats cat) {
		this.setId(cat.getId());
		this.setTaskId(cat.getTaskId());
		this.cat = cat;
	}
	
	public void processSummary(Double lower, Double upper) {
		Map<Integer, Long> result = new HashMap<Integer, Long>();
		for(Entry<Integer, Hist> key : this.cat.getCats().getCategories().entrySet()) {
			Stats s = null;
			for(Bucket b : key.getValue().getBuckets()) {
				if(b.getLower() >= lower && b.getUpper() <= upper) {
					if(s == null)
						s = new Stats();
					s.fold(b.getStats());
				}
			}
			if(s != null)
				// TODO : remove hard coded resolution
				// replace it to outputResolution.
				result.put(key.getKey(), s.getCount() * 25 * 25);
		}
		this.setCatSummaries(result);
	}
	
//	public TaskCats toTaskCats() {
//		TaskCats tc = new TaskCats(getTaskId(), getName(), getOutputResolution(),getCats());
//		return tc;
//	}
}
