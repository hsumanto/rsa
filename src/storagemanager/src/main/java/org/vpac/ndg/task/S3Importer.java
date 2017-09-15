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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vpac.ndg.ApplicationContextProvider;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.storage.dao.BandDao;
import org.vpac.ndg.storage.dao.TimeSliceDao;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.util.TimeSliceUtil;

/**
* This is a tool to import user specified datasets from AWS d3 storage.
*
* @author sean
*
*/
public class S3Importer extends Application {

  final private Logger log = LoggerFactory.getLogger(Importer.class);

  private String bucket;
  private String key;
  private Band band;

	TimeSliceDao timeSliceDao;
  TimeSliceUtil timeSliceUtil;
	BandDao bandDao;

  public S3Importer() {
    ApplicationContext appContext = ApplicationContextProvider.getApplicationContext();
    timeSliceDao = (TimeSliceDao) appContext.getBean("timeSliceDao");
		timeSliceUtil = (TimeSliceUtil) appContext.getBean("timeSliceUtil");
		bandDao = (BandDao) appContext.getBean("bandDao");
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
	protected void initialise() throws TaskInitialisationException {
    super.initialise();

    this.finalise();
  }

  @Override
	protected void createTasks() throws TaskInitialisationException {
    // TASK 1: Download tiles from s3
    S3Download s3Download = new S3Download();
    s3Download.setBucketName(bucket);
    s3Download.setKey(key);

    // TASK 2: Commit tiles to database
    // Committer committer = new Committer();

    // Add tasks to task pipeline
    getTaskPipeline().addTask(s3Download);
    // getTaskPipeline().addTask(committer);
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
		if (key != null && bucket != null) {
				return String.format("Importing %s from s3 bucket: %s", key, bucket);
		} else {
			return String.format("Importing %s", key);
		}
	}
}
