package org.vpac.ndg.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.vpac.ndg.CommandUtil;
import org.vpac.ndg.FileUtils;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.StringUtils;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.rasterservices.ProcessException;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * Class uses the gdal2tiles.py python script (run as command line app) to produce a set of WMTS
 * compatible image tiles
 * @author lachlan
 *
 */
public class TileBuilder extends Task {

    private GraphicsFile source;
    private Path target;  //in this case the target is simply a directory
    private String profile;
    private int zoomMax = 7;
    private int zoomMin = 0;

    private CommandUtil commandUtil;
    final private Logger log = LoggerFactory.getLogger(TileBuilder.class);

    public TileBuilder() {
        this("Building tiles");
    }

    public TileBuilder(String description) {
        super(description);
        commandUtil = new CommandUtil();
    }

    @Override
    public void initialise() throws TaskInitialisationException {
        if (getSource() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_NO_INPUT_IMAGES);
        }

        if (getTarget() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_TARGET_DATASET_NOT_SPECIFIED);
        }

        if (getTarget().toFile().exists()) {
            //then delete it, will be old data
            try {
                FileUtils.removeDirectory(getTarget());
            } catch (IOException e) {
                throw new TaskInitialisationException("Unable to delete old WMTS tiles directory", e);
            }
        }

        boolean created = getTarget().toFile().mkdirs();
        if (!created) {
            throw new TaskInitialisationException("Could not create directory for output WMTS tiles");
        }

    }

    public List<String> prepareCommand() {
        List<String> command = new ArrayList<String>();

        // get the input file list
        command.add("gdal2tiles.py");

        if (profile != null) {
            command.add("-p");
            command.add(profile);
        }

        //add zoom argument
        command.add("-z");
        command.add(Integer.toString(this.zoomMin) + "-" + Integer.toString(this.zoomMax));

        command.add(source.getFileLocation().toString());
        command.add(target.toString());

        log.info("gdal2tile:" + command);
        return command;
    }

    @Override
    public void execute(Collection<String> actionLog, IProgressCallback progressCallback) throws TaskException {

        List<String> command = prepareCommand();
		actionLog.add(StringUtils.join(command, " "));
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            //gdal2tiles runs from 0-100 twice, once for base tiles, and once for overlay tiles
            //to count progress all we do is sum up the total number of dots printed to stdout
            // there should be a total of 62, including the ones after "done".
            int progressInt = 0;
            int totalFullStops = 62;

            InputStreamReader r = new InputStreamReader(process.getInputStream());
            int intch;

            while ((intch = r.read()) != -1) {
                char ch = (char) intch;
                if (ch == '.') {
                    progressInt += 1;
                }
                double progressPercentage = (double)progressInt/(double)totalFullStops;
                if (progressCallback != null) {
                    progressCallback.progressUpdated(progressPercentage);
                }
            }
        } catch (IOException e) {
            throw new TaskException(getDescription(), e);
        }
    }

    @Override
    public void rollback() {
        // nothing to do

    }

    @Override
    public void finalise() {
        // nothing to do

    }


    public int getZoomMax() {
        return zoomMax;
    }

    public void setZoomMax(int zoomMax) {
        this.zoomMax = zoomMax;
    }

    public int getZoomMin() {
        return zoomMin;
    }

    public void setZoomMin(int zoomMin) {
        this.zoomMin = zoomMin;
    }

    public GraphicsFile getSource() {
        return source;
    }

    public void setSource(GraphicsFile source) {
        this.source = source;
    }

    public Path getTarget() {
        return target;
    }

    public void setTarget(Path target) {
        this.target = target;
    }

    public String getProfile() {
        return profile;
    }

    /**
     * Tile cutting profile (mercator,geodetic,raster) - default 'mercator' (Google Maps compatible)
     * @param process
     */
    public void setProfile(String process) {
        this.profile = process;
    }






}
