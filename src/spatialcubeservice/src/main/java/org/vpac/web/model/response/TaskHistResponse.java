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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.storage.model.TaskHist;

@XmlRootElement(name = "TaskHist")
public class TaskHistResponse {
	private String id;
	private String taskId;
	private String name;
	private Hist hist;
	
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

	public Hist getHist() {
		return this.hist;
	}
	@XmlAttribute
	public void setHist(Hist hist) {
		this.hist = hist;
	}
	public String getName() {
		return name;
	}
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	
	public TaskHistResponse() {
	}
	
	public TaskHistResponse(TaskHist hist) {
		this.setId(hist.getId());
		this.setTaskId(hist.getTaskId());
		this.setHist(hist.getHist());
	}
	
	public TaskHist toTaskHist() {
		TaskHist th = new TaskHist(getTaskId(), getName(), getHist());
		return th;
	}
}
