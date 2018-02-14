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
public class TileBuilder extends BaseTask {

    final static String DEFAULT_GDAL2TILES_COMMAND = "gdal2tiles.py";

    private GraphicsFile source;
    private Path target;  //in this case the target is simply a directory
    private String profile;

    private boolean zoomLevelsEnabled = false;
    private int zoomMax = 7;
    private int zoomMin = 0;

    private CommandUtil commandUtil;
    final private Logger log = LoggerFactory.getLogger(TileBuilder.class);

    private String gdal2tilesCommand;

    public TileBuilder() {
        this("Building tiles");
    }

    public TileBuilder(String description) {
        super(description);
        commandUtil = new CommandUtil();
        gdal2tilesCommand = TileBuilder.DEFAULT_GDAL2TILES_COMMAND;
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
        command.add(gdal2tilesCommand);

        if (profile != null) {
            command.add("-p");
            command.add(profile);
        }

        if (this.zoomLevelsEnabled) {
            //add zoom argument
            command.add("-z");
            command.add(Integer.toString(this.zoomMin) + "-" + Integer.toString(this.zoomMax));
        }

        command.add(source.getFileLocation().toString());
        command.add(target.toString());

        log.info("gdal2tile:" + command);
        return command;
    }

    @Override
    public void execute(Collection<String> actionLog, ProgressCallback progressCallback) throws TaskException {

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

            process.waitFor();

            int processReturnValue = process.exitValue();
            if (processReturnValue != 0) {
                String message = " non-zero return value (" + Integer.toString(processReturnValue) + ")";
                throw new TaskException(getDescription() + message);
            }
        } catch (InterruptedException e) {
            throw new TaskException(getDescription(), e);
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

    /*
     * Passing the zoom levels arguement to gdal2tiles is disabled by default.
     */
    public void enableZoomLevels() {
        this.zoomLevelsEnabled = true;
    }

    public void disableZoomLevels() {
        this.zoomLevelsEnabled = false;
    }


    public int getZoomMax() {
        return zoomMax;
    }

    /**
     * Sets the maximum zoom level that will be generated.
     * Note: setting this will enable the zoom level arguement to gdal
     */
    public void setZoomMax(int zoomMax) {
        this.zoomLevelsEnabled = true;
        this.zoomMax = zoomMax;
    }

    public int getZoomMin() {
        return zoomMin;
    }

    /**
     * Sets the minimum zoom level that will be generated.
     * Note: setting this will enable the zoom level arguement to gdal
     */
    public void setZoomMin(int zoomMin) {
        this.zoomLevelsEnabled = true;
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

    public void setGdal2TilesCommand(String command) {
        this.gdal2tilesCommand = command;
    }

    public String getGdal2TilesCommand() {
        return this.gdal2tilesCommand;
    }




}
