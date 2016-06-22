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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vpac.ndg.ApplicationContextProvider;
import org.vpac.ndg.CommandUtil;
import org.vpac.ndg.FileUtils;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.StringUtils;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.rasterservices.ProcessException;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.TileBand;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.util.TimeSliceUtil;
import org.vpac.ndg.storagemanager.GraphicsFile;
import org.vpac.ndg.task.OutputDirStatistics;

public class VrtBuilder extends Task {

    final private Logger log = LoggerFactory.getLogger(VrtBuilder.class);

    private List<TileBand> source;
    private GraphicsFile sourceFile;
    private GraphicsFile target;
    private List<GraphicsFile> outputBucket;
    private Path temporaryLocation;
    private TimeSlice timeSlice;
    private Band band;
    private List<Path> tmpFileList; // Store the tmp vrt files in tmp directory
    TimeSliceUtil timeSliceUtil;
    private boolean copyToStoragePool;

    private boolean targetResolutionSet;
    private double targetResolutionX;
    private double targetResolutionY;

    private boolean targetExtentsSet;
    private double[] targetExtents;
    private ScalarReceiver<OutputDirStatistics> outputDirStatistics;

    private CommandUtil commandUtil;

    public VrtBuilder() {
        this(Constant.TASK_DESCRIPTION_VRTBUILDER);
    }

    public VrtBuilder(String description) {
        super(description);
        ApplicationContext appContext = ApplicationContextProvider
                .getApplicationContext();
        timeSliceUtil = (TimeSliceUtil) appContext.getBean("timeSliceUtil");
        commandUtil = new CommandUtil();
        copyToStoragePool = false;

        targetResolutionSet = false;
        targetExtentsSet = false;
    }

    @Override
    public void initialise() throws TaskInitialisationException {
        if (getSource() == null && this.sourceFile == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_NO_INPUT_IMAGES);
        }

        if (getTarget() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_TARGET_DATASET_NOT_SPECIFIED);
        }

        // If temporaryLocation is null, create temporary location
        if (temporaryLocation == null) {
            try {
                temporaryLocation = FileUtils.createTmpLocation();
            } catch (IOException e) {
                log.error("Could not create temporary directory: {}", e);
                throw new TaskInitialisationException(
                        String.format(
                                "Error encountered when create temporary directory: %s",
                                temporaryLocation));
            }
            log.info("Temporary Location: {}", temporaryLocation);
        }

        tmpFileList = new ArrayList<Path>();
    }

    public void revalidateBeforeExecution() throws TaskException {
        if (source != null) {
            for (TileBand tileband : source) {
                if (!tileband.getBand().equals(band)) {
                    throw new TaskException(String.format(
                            "Band \"%s\" not found in dataset.", tileband.getBand()
                                    .getName()));
                }
            }
        }

    }

    public List<String> prepareCommand() {
        List<String> command = new ArrayList<String>();

        // get the input file list
        command.add("gdalbuildvrt");

        command.add("-overwrite");

        if (targetResolutionSet) {
            command.add("-tr");
            command.add(Double.toString(targetResolutionX));
            command.add(Double.toString(targetResolutionY));
        } else {
            command.add("-resolution");
            command.add("highest");
        }

        if (targetExtentsSet) {
            command.add("-te");
            for (double d : targetExtents) {
                command.add(Double.toString(d));
            }
        }

        if (band !=null && band.getNodata() != null && band.getNodata().length() != 0) {
            log.info("No data :" + band.getNodata());
            command.add("-srcnodata");
            command.add(band.getNodata());
        } else if (outputDirStatistics != null) {
            OutputDirStatistics os = outputDirStatistics.get();
            log.info("getPixelType:" + os.getPixelType().get());
            if (os.getPixelType().get() != null) {
                command.add("-srcnodata");
                Double srcNodata = os.getNodata().get();
                if (os.getPixelType().get().equals("SIGNEDBYTE")) {
                    // Convert to byte value
                    // For example -1 nodata value not working for vrtbuilder
                    // when output file's nodata type is byte and it's over
                    // the boundary of byte, need to be convert
                    int nodataValue = srcNodata.byteValue() & 0xFF;
                    command.add(Integer.toString(nodataValue));
                } else {
                    command.add(srcNodata.toString());
                }

                command.add("-vrtnodata");
                command.add(os.getNodata().get().toString());
            }
        }

        command.add(target.getFileLocation().toString());

        if (source != null) {
            for (TileBand tileband : source) {
                command.add(tileband.getFileLocation().toString());
            }
        } else  {
            List<Path> sourceFiles = getSourceFilesFromFile(sourceFile);
            for (Path sourceFile: sourceFiles) {
                command.add(sourceFile.toString());
            }
        }

        log.info("command:" + command);
        return command;
    }

    private List<Path> getSourceFilesFromFile(GraphicsFile file) {
        List<Path> sourceFiles = new ArrayList<Path>();
        if (file.getFileLocation().toFile().isDirectory())
        {
            File[] filesInDir = file.getFileLocation().toFile().listFiles();
            for (File fileInDir : filesInDir) {
                if (fileInDir.isFile() && fileInDir.toPath() != file.getFileLocation()) {
                    sourceFiles.add(fileInDir.toPath());
                }
            }
        } else {
             sourceFiles.add(sourceFile.getFileLocation());
        }
        return sourceFiles;
    }


    @Override
    public void execute(Collection<String> actionLog, IProgressCallback progressCallback) throws TaskException {
        if (source == null && sourceFile == null && source.isEmpty()) {
            // Can't work with zero input files. Just return; the output list
            // will not be populated. This is not an error.
            log.debug("Source is empty; will not create a VRT.");
            return;
        }

        revalidateBeforeExecution();

        // Prepare gdalbuildvrt for the specified band and then execute it
        List<String> command = prepareCommand();
		actionLog.add(StringUtils.join(command, " "));
        try {
            commandUtil.start(command);
        } catch (ProcessException e) {
            throw new TaskException(getDescription(), e);
        } catch (InterruptedException e) {
            throw new TaskException(getDescription(), e);
        } catch (IOException e) {
            throw new TaskException(getDescription(), e);
        }

        if (outputBucket != null)
            outputBucket.add(target);

        // TODO: Copying should always be done in the Committer, not here.
        if (!isCopyToStoragePool())
            return;

        try {
            // Initialize timeSlice directory in storagepool if applicable
            timeSliceUtil.initializeLocations(timeSlice);
        } catch (IOException e) {
            log.error("Could not create timeslice directory: {}", e);
            throw new TaskException(String.format(
                    "Error encountered when creating timeslice directory: %s",
                    timeSliceUtil.getFileLocation(timeSlice)));
        }
        Path from = target.getFileLocation();
        Path to = timeSliceUtil.getFileLocation(timeSlice).resolve(
                from.getFileName());
		actionLog.add(String.format("cp '%s' '%s'", from, to));
        try {
            // Store the tmp vrt file for removal later
            tmpFileList.add(from);
            // Copy into vrt file into storage pool
            FileUtils.copy(from, to);
            // Set the new location in storage pool
            target.setFileLocation(to);
        } catch (IOException e) {
            throw new TaskException(String.format(
                    Constant.ERR_COPY_FILE_FAILED, from, to), e);
        }
    }

    @Override
    public void rollback() {
        if (target == null) {
            return;
        }

        // Delete vrt file
        if (target.deleteIfExists()) {
            log.trace("Deleted {}", target);
        }
    }

    @Override
    public void finalise() {
        for (Path tmpVrt : tmpFileList) {
            // Delete vrt file in temporary storage
            if (FileUtils.deleteIfExists(tmpVrt)) {
                log.trace("Deleted {}", tmpVrt);
            }
        }
    }

    /**
     * set the target resolution flag (-tr) passed to gdalbuildvrt, this will
     * prevent the '-resolution' arguement from being included in the command
     * line (obviously as the gdal docs state)
     *
     * @param resolutionX
     * @param resolutionY
     */
    public void setTargetResolution(double resolutionX, double resolutionY) {
        targetResolutionSet = true;
        targetResolutionX = resolutionX;
        targetResolutionY = resolutionY;
    }

    /**
     * set the target extents flag (-te) passed to gdalbuildvrt
     *
     * @param xmin
     * @param ymin
     * @param xmax
     * @param ymax
     */
    public void setTargetExtents(double xmin, double ymin, double xmax,
            double ymax) {
        double te[] = { xmin, ymin, xmax, ymax };
        targetExtentsSet = true;
        targetExtents = te;
    }

    public void setSource(List<TileBand> source) {
        this.source = source;
    }

    /**
     * Sets a graphics file as the source to use in the construction of a VRT file
     * @param source
     */
    public void setSource(GraphicsFile source) {
        this.sourceFile = source;
    }

    public List<TileBand> getSource() {
        return source;
    }

    public void setTarget(GraphicsFile vrtFile) {
        this.target = vrtFile;
    }

    public GraphicsFile getTarget() {
        return target;
    }

    public void setTimeSlice(TimeSlice timeSlice) {
        this.timeSlice = timeSlice;
    }

    public TimeSlice getTimeSlice() {
        return timeSlice;
    }

    public void setOutputDirStatistics(ScalarReceiver<OutputDirStatistics> outputDirStatistics) {
        this.outputDirStatistics = outputDirStatistics;
    }

    public ScalarReceiver<OutputDirStatistics> getOutputDirStatistics() {
        return outputDirStatistics;
    }

    /**
     * Set the temporary location for storing temporary .ncml file.
     *
     * @param temporaryLocation
     *            The specified temporary location.
     */
    public void setTemporaryLocation(Path temporaryLocation) {
        this.temporaryLocation = temporaryLocation;
    }

    public Band getBand() {
        return band;
    }

    public void setBand(Band band) {
        this.band = band;
    }

    public List<Path> getTmpFileList() {
        return tmpFileList;
    }

    public boolean isCopyToStoragePool() {
        return copyToStoragePool;
    }

    public void setCopyToStoragePool(boolean copyToStoragePool) {
        this.copyToStoragePool = copyToStoragePool;
    }

    public List<GraphicsFile> getOutputBucket() {
        return outputBucket;
    }

    public void setOutputBucket(List<GraphicsFile> outputBucket) {
        this.outputBucket = outputBucket;
    }
}
