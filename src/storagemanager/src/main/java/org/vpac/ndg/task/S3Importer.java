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

package org.vpac.ndg.task;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.Format;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.TimeSlice;
/**
* This is a tool to import user specified datasets from AWS d3 storage.
*
* @author sean
*
*/
public class S3Importer extends Application {

  final private Logger log = LoggerFactory.getLogger(Importer.class);

  private ArrayList<String> files;
  private String bucket;
  private String key;
  private Dataset dataset;
  private TimeSlice timeSlice;
  private Band band;
  private CellSize dsResolution;
  private Format fileFormat;

  public S3Importer() {
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public void setResolution(CellSize resolution) {
    dsResolution = resolution;
  }

  public void setTimeSlice(TimeSlice timeSlice) {
    this.timeSlice = timeSlice;
  }

  public void setBand(Band band) {
    this.band = band;
  }

  public void setFileFormat(Format format) {
    fileFormat = format;
  }

  public void setS3Targets(ArrayList<String> files) {
    this.files = files;
  }

  @Override
	protected void createTasks() throws TaskInitialisationException {
    // TASK: Download tiles directly into storagepool from s3 bucket
    S3Download s3Download = new S3Download();
    s3Download.setTemporaryLocation(getWorkingDirectory());
    s3Download.setDataset(dataset);
    s3Download.setResolution(dsResolution);
    s3Download.setTimeSlice(timeSlice);
    s3Download.setBand(band);
    s3Download.setFileFormat(fileFormat);

    s3Download.setBucketName(bucket);
    s3Download.setTargetFiles(files);

    getTaskPipeline().addTask(s3Download);
	}

  @Override
  protected void finalise() {
    super.finalise();
  }

  @Override
  protected String getJobName() {
    return Constant.TOOL_IMPORT;
  }

  @Override
  protected TaskType getTaskType() {
		return TaskType.Import;
	}

  @Override
	protected String getJobDescription() {
		if (bucket != null) {
				return String.format("Importing tiles from s3 bucket: %s", bucket);
		} else {
			return String.format("Importing tiles from s3");
		}
	}
}
