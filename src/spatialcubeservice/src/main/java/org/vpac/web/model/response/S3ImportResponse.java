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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.model.Band;


@XmlRootElement(name = "S3Import")
public class S3ImportResponse {
	private String taskId;
	private String datasetId;
  private String timesliceId;
  private String bandId;

  @XmlElement
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @XmlElement
  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  @XmlElement
  public void setTimesliceId(String timesliceId) {
    this.timesliceId = timesliceId;
  }

  @XmlElement
  public void setBandId(String bandId) {
    this.bandId = bandId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public String getTimesliceId() {
    return timesliceId;
  }

  public String getBandId() {
    return bandId;
  }

  public S3ImportResponse(String taskId, Dataset d, TimeSlice t, Band b) {
      this.setTaskId(taskId);
      if (d != null){
        this.setDatasetId(d.getId());
      }
      if (t != null){
        this.setTimesliceId(t.getId());
      }
      if (b != null){
        this.setBandId(b.getId());
      }
  }

  public S3ImportResponse() {
  }
}
