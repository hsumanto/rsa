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

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;

/**
 * The task of this class is to download tiles stored in an s3 bucket.
 * @author sean
 *
 */
public class S3Download extends BaseTask {

  final private Logger log = LoggerFactory.getLogger(S3Download.class);

  private String bucketName;
  private String key;

  public S3Download() {
    super(Constant.TASK_DESCRIPTION_S3DOWNLOAD);
    log.info("S3Download task constructed");
  }

  @Override
	public void initialise() throws TaskInitialisationException {
    log.info("S3Download task initialized");
  }

  @Override
	public void execute(Collection<String> actionLog, ProgressCallback progressCallback) throws TaskException {
    String s3Filename = "/tmp/" + bucketName + "/" + key;
    String localFilename = "/var/lib/ndg/storagepool/" + key;
    Path path = Paths.get(localFilename);

    if (Files.exists(path)) {
      // Handle case that file has already been downloaded to RSA
      log.info(String.format("File %s already exists", localFilename));
      return;
    }

    Path parentPath = path.getParent();
    try {
      Files.createDirectories(parentPath);
    } catch (IOException i) {
      throw new TaskException(String.format(
        "Caught an IOException while trying to create directories %s",
        parentPath.toString()), i);
    }

    // This role must have permission to access the s3 bucket.
    AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
    try {
      S3Object s3object = s3Client.getObject(new GetObjectRequest(bucketName, key));
      log.info("Content-Type: " + s3object.getObjectMetadata().getContentType());

      GetObjectRequest objectRequest = new GetObjectRequest(bucketName, key);
      S3Object objectPortion = s3Client.getObject(objectRequest);
      InputStream objectData = objectPortion.getObjectContent();

      try {
        Files.copy(objectData, path);
      } catch (IOException i) {
        throw new TaskException(String.format(
          "Caught an IOException while trying to copy tile data to %s",
          localFilename), i);
      }

      try {
        objectPortion.close();
      } catch (IOException i) {
        throw new TaskException(
        "Caught an IOException while attempting to close S3Object",
        i);
      }

    } catch (AmazonServiceException ase) {
      log.info("Caught an AmazonServiceException, which" +
          " means your request made it " +
          "to Amazon S3, but was rejected with an error response" +
          " for some reason.");
      log.info("Error Message:    " + ase.getMessage());
      log.info("HTTP Status Code: " + ase.getStatusCode());
      log.info("AWS Error Code:   " + ase.getErrorCode());
      log.info("Error Type:       " + ase.getErrorType());
      log.info("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      log.info("Caught an AmazonClientException, which means"+
          " the client encountered " +
          "an internal error while trying to " +
          "communicate with S3, " +
          "such as not being able to access the network.");
      log.info("Error Message: " + ace.getMessage());
    }
  }

  @Override
  public void rollback() {
    log.info("S3Download rollback");
  }

  @Override
  public void finalise() {
    log.info("S3Download finalise");
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
