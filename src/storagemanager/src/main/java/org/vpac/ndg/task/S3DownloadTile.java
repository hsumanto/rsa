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
import java.nio.file.StandardCopyOption;
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
 * The task of this class is to download a tile from an s3 bucket.
 * @author sean
 *
 */
public class S3DownloadTile extends BaseTask {

  final private Logger log = LoggerFactory.getLogger(S3DownloadTile.class);

  private String bucketName;
  private String key;
  private Path storagePool;

  public S3DownloadTile() {
    super(Constant.TASK_DESCRIPTION_S3DOWNLOADTILE);
  }

  @Override
	public void initialise() throws TaskInitialisationException {
    if (bucketName == null) {
      throw new TaskInitialisationException(getDescription(), Constant.ERR_S3_BUCKET_NOT_SPECIFIED);
    }

    if (key == null) {
      throw new TaskInitialisationException(getDescription(), Constant.ERR_S3_KEY_NOT_SPECIFIED);
    }

    if(storagePool == null) {
      throw new TaskInitialisationException(getDescription(), Constant.ERR_STORAGEPOOL_NOT_SPECIFIED);
    }
  }

  @Override
  public void execute(Collection<String> actionLog, ProgressCallback progressCallback) throws TaskException {
    Path path = Paths.get(storagePool.toString(), key);

    // This role must have permission to access the s3 bucket.
    AmazonS3 s3Client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
    try {
      S3Object s3object = s3Client.getObject(new GetObjectRequest(bucketName, key));

      GetObjectRequest objectRequest = new GetObjectRequest(bucketName, key);
      S3Object objectPortion = s3Client.getObject(objectRequest);
      InputStream objectData = objectPortion.getObjectContent();

      try {
        Files.copy(objectData, path, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new TaskException(String.format(
          "Caught an IOException while trying to copy tile data to %s",
          path.toString()), e);
      }

      try {
        objectPortion.close();
      } catch (IOException e) {
        throw new TaskException(
        "Caught an IOException while attempting to close S3Object",
        e);
      }

    } catch (AmazonServiceException ase) {
      log.info("Caught an AmazonServiceException, which" +
          " means your request made it" +
          " to Amazon S3, but was rejected with an error response" +
          " for some reason.");
      log.info("Error Message:    " + ase.getMessage());
      log.info("HTTP Status Code: " + ase.getStatusCode());
      log.info("AWS Error Code:   " + ase.getErrorCode());
      log.info("Error Type:       " + ase.getErrorType());
      log.info("Request ID:       " + ase.getRequestId());
      throw new TaskException(String.format("Failed to retrieve %s due to AmazonServiceException", key));
    } catch (AmazonClientException ace) {
      log.info("Caught an AmazonClientException, which means"+
          " the client encountered" +
          " an internal error while trying to" +
          " communicate with S3," +
          " such as not being able to access the network.");
      log.info("Error Message: " + ace.getMessage());
      throw new TaskException(String.format("Failed to retrieve %s due to AmazonClientException", key));
    }
  }

  @Override
  public void rollback() {
    // Delete file if it exists in storagepool
    Path path = Paths.get(storagePool.toString(), key);

    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  @Override
  public void finalise() {
  }

  public void setStoragePool(Path storagePool) {
    this.storagePool = storagePool;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
