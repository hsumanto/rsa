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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.FileUtils;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.Format;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;

/**
 * The task of this class is to download tiles stored in an s3 bucket.
 * @author sean
 *
 */
public class S3Download extends BaseTask {

  final private Logger log = LoggerFactory.getLogger(S3Download.class);

  private TaskPipeline innerTaskPipeline = new TaskPipeline(false);
  private ArrayList<String> tgtFiles;
  private String bucketName;
  private String dsName;
  private CellSize dsResolution;
  private String tsName;
  private String bandName;
  private Format fileFormat;
  private Path storagePoolDir;
  private Path temporaryLocation;

  public S3Download() {
    super(Constant.TASK_DESCRIPTION_S3DOWNLOAD);
  }

  @Override
	public void initialise() throws TaskInitialisationException {
    if (bucketName == null) {
      throw new TaskInitialisationException(getDescription(), Constant.ERR_S3_BUCKET_NOT_SPECIFIED);
    }

    if (tgtFiles == null || tgtFiles.isEmpty()) {
      throw new TaskInitialisationException(getDescription(), Constant.ERR_S3_TARGET_FILES_NOT_SPECIFIED);
    }

    if (dsName == null) {
      throw new TaskInitialisationException(getDescription(), "Dataset name not specified");
    }

    if (dsResolution == null) {
      throw new TaskInitialisationException(getDescription(), "Dataset resolution not specified");
    }

    if (tsName == null) {
      throw new TaskInitialisationException(getDescription(), "Timeslice name not specified");
    }

    if (bandName == null) {
      throw new TaskInitialisationException(getDescription(), "Band name not specified");
    }

    if (fileFormat == null) {
      throw new TaskInitialisationException(getDescription(), "File format not specified");
    }

    if (storagePoolDir == null) {
      throw new TaskInitialisationException(getDescription(), "Storage pool directory not specified");
    }

    if(temporaryLocation == null) {
      try {
        temporaryLocation = FileUtils.createTmpLocation();
      } catch (IOException e) {
        log.error("Could not create temporary directory: {}", e);
        throw new TaskInitialisationException(String.format("Error encountered when create temporary directory: %s", temporaryLocation));
      }
      log.info("Temporary Location: {}", temporaryLocation);
    }
  }

  @Override
	public void execute(Collection<String> actionLog, ProgressCallback progressCallback) throws TaskException {
    innerTaskPipeline.setActionLog(actionLog);

    // Create download task for each target tile
    tgtFiles.forEach((f) -> createDownloadTask(f));

    try {
      innerTaskPipeline.initialise();
    } catch (TaskInitialisationException e) {
      throw new TaskException(e);
    }

    if (Files.exists(storagePoolDir)) {
      try {
        // Move existing files for this band and file format to temporary storage.
        String namePattern = bandName + "_tile*" + fileFormat.getExtension();
        DirectoryStream<Path> ds = Files.newDirectoryStream(storagePoolDir, namePattern);
        for (Path from: ds) {
          Path to = temporaryLocation.resolve(from.getFileName());
          try {
            FileUtils.move(from, to);
          } catch (IOException e) {
            throw new TaskException(String.format(Constant.ERR_MOVE_FILE_FAILED, from, to));
          }
        }
      } catch (IOException e) {
        throw new TaskException(String.format("Could not create directory stream for %s", storagePoolDir.toString()));
      }
    } else {
      // Create storage pool directory
      try {
        Files.createDirectories(storagePoolDir);
      } catch (IOException e) {
        throw new TaskException(String.format(
          "Caught an IOException while trying to create directories %s",
          storagePoolDir.toString()), e);
      }
    }

    // Run tile download tasks
    innerTaskPipeline.run();
  }

  @Override
  public void rollback() {
    // Rollback each of the tile download tasks
    innerTaskPipeline.rollback();

    // Restore original tiles in temporary storage back to storagepool
    try {
      DirectoryStream<Path> ds = Files.newDirectoryStream(temporaryLocation);
      for (Path from: ds) {
        Path to = storagePoolDir.resolve(from.getFileName());
        try {
          FileUtils.move(from, to);
        } catch (IOException e) {
          log.error(String.format(Constant.ERR_MOVE_FILE_FAILED, from, to));
        }
      }
    } catch(IOException e) {
      log.error(String.format("Could not create directory stream for %s", storagePoolDir.toString()));
    }
  }

  @Override
  public void finalise() {
    // Finalise each of the tile download tasks
    innerTaskPipeline.finalise();
  }

  protected void createDownloadTask(String tgtFile) {
    // Create a new download task for this file
    String key = dsName + "_" + dsResolution + "/" + tsName + "/" + tgtFile;
    Path storagePool = Paths.get("/var/lib/ndg/storagepool/");
    S3DownloadTile tileDownload = new S3DownloadTile();
    tileDownload.setStoragePool(storagePool);
    tileDownload.setBucketName(bucketName);
    tileDownload.setKey(key);

    // Add download task to inner task pipeline
    innerTaskPipeline.addTask(tileDownload);
  }

  public void setTemporaryLocation(Path temporaryLocation) {
		this.temporaryLocation = temporaryLocation;
  }

  public Path getTemporaryLocation() {
		return temporaryLocation;
	}

  public void setStoragePoolDir(String dirPath) {
    storagePoolDir = Paths.get(dirPath);
  }

  public void setDatasetName(String name) {
    dsName = name;
  }

  public void setResolution(CellSize resolution) {
    dsResolution = resolution;
  }

  public void setTimeSliceName(String name) {
    tsName = name;
  }

  public void setBandName(String name) {
    bandName = name;
  }

  public void setFileFormat(Format format) {
    fileFormat = format;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public void setTargetFiles(ArrayList<String> files) {
    this.tgtFiles = files;
  }
}
