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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Stats;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskHist;

@XmlRootElement(name = "TaskHist")
public class TaskHistResponse {
	private String id;
	private String taskId;
	private String name;
	private Cats cat;
	private List<HistElement> histSummaries;
	
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
	public List<HistElement> getHistSummaries() {
		return histSummaries;
	}
	@XmlAttribute
	public void setHistSummaries(List<HistElement> histSummaries) {
		this.histSummaries = histSummaries;
	}
	
	public TaskHistResponse() {
	}
	
	public TaskHistResponse(TaskCats cat) {
		this.setId(cat.getId());
		this.setTaskId(cat.getTaskId());
		this.cat = cat.getCats();
	}
	
	public void processSummary(List<Integer> categories) {
		List<HistElement> result = new ArrayList<HistElement>();
		Hist histSummary = new Hist();
		for(Hist h : cat.getCategories().values()) {
			histSummary.fold(h);
		}
		for(Bucket b : histSummary.getBuckets()) {
			result.add(new HistElement(b.getLower(), b.getUpper(), b.getStats().getCount()));
		}
		setHistSummaries(result);
	}
//	public TaskHist toTaskHist() {
//		TaskHist th = new TaskHist(getTaskId(), getName(), getHist());
//		return th;
//	}
}