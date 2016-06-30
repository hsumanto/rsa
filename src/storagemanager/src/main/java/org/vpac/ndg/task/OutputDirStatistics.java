package org.vpac.ndg.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FilenameFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.nio.file.Path;
import java.lang.Math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.StringUtils;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * Class uses gdalinfo to extract some statistics from the source dataset. Makes use
 * of ScalarRecievers to pass the information provided by this task to other tasks.
 * @author lachlan
 *
 */
public class OutputDirStatistics extends BaseTask {

    final private Logger log = LoggerFactory.getLogger(OutputDirStatistics.class);

    //key strings used to find stats in gdalinfo output
    private final String STATS_MAX = "STATISTICS_MAXIMUM";
    private final String STATS_MIN = "STATISTICS_MINIMUM";
    private final String PIXEL_TYPE = "PIXELTYPE";
    private final String NO_DATA = "NoData Value";
    private final String OUTPUT_FILE_FORMAT = ".nc";

    private Path sourceDir;
    private boolean approximate;

    //the gathered stats
    private ScalarReceiver<Double> max;
    private ScalarReceiver<Double> min;
    private ScalarReceiver<Double> nodata;
    private ScalarReceiver<String> pixelType;



    public OutputDirStatistics() {
        this("Extracting statistics from output dir");
    }

    public OutputDirStatistics(String description) {
        super(description);
    }

    @Override
    public void initialise() throws TaskInitialisationException {
        if (getSource() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_NO_INPUT_IMAGES);
        }

    }

    public List<String> prepareCommand() {
        List<String> command = new ArrayList<String>();

        // get the input file list
        command.add("gdalinfo");

        command.add("-noct");
        // command.add("-mm");

        if (approximate) {
            command.add("-approx_stats");
        } else {
            command.add("-stats");
        }

        // command.add(source.getFileLocation().toString());
        // log.info("command:" + command);
        return command;
    }



    @Override
    public void execute(Collection<String> actionLog, ProgressCallback progressCallback) throws TaskException {

        try {

            File dir = new File(sourceDir.toString());
            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(OUTPUT_FILE_FORMAT);
                }
            });

            for(File filePath : files) {
                String stdout = "";
                List<String> command = prepareCommand();

                String listString = "";
                for (String s : command) {
                    listString += s + " ";
                }

                listString += filePath;
                command.add(filePath.toString());
                log.info("command:" + command);
                //in this case we dont use the command util as we care about the stdout stuff
                actionLog.add(StringUtils.join(command, " "));
                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb.start();

                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ( (line = br.readLine()) != null) {
                   builder.append(line);
                   builder.append(System.lineSeparator());
                }
                String result = builder.toString();
                stdout = result;

                String lines[] = stdout.split(System.lineSeparator());
                Double value;
                ScalarReceiver<Double> sr;

                for (String line2: lines) {
                    line2 = line2.trim();
                    if (line2.startsWith(STATS_MAX)) {
                        //log.info("Found max of " + getStatsValue(line2));
                        if (max == null || max.get() == null)
                            getMax().set(getStatsValue(line2));
                        else
                            getMax().set(Math.max(max.get(), getStatsValue(line2)));
                    } else if (line2.startsWith(STATS_MIN)) {
                        //log.info("Found min of " + getStatsValue(line2));
                        if (min == null || min.get() == null)
                            getMin().set(getStatsValue(line2));
                        else
                            getMin().set(Math.min(min.get(), getStatsValue(line2)));
                    } else if (line2.startsWith(PIXEL_TYPE)) {
                        getPixelType().set(getPixelTypeValue(line2));
                        //log.info("Found pixel type of " + getPixelType().get());
                    } else if (line2.startsWith(NO_DATA)) {
                        getNodata().set(getStatsValue(line2));
                        //log.info("Found No data value of " + getNodata().get());
                    }
                }
            }

            log.info("Aggregated Min: " + min.get());
            log.info("Aggregated Max: " + max.get());
            // Check the madatory parameter is set
            if (min == null || max == null || nodata == null)
                throw new TaskException("Four mandatory field is not set");

        } catch (TaskException te) {
           throw te;
        } catch (IOException ioe) {
            throw new TaskException(getDescription(), ioe);
        }
    }


    /**
     * gets the value that comes after the equals sign
     * @param line
     * @return
     */
    private Double getStatsValue(String line) {
        String numberBit = line.substring(line.indexOf('=')+1);
        return Double.parseDouble(numberBit);
    }

    private void setComputedValues(String line) {
        String minAndMax = line.substring(line.indexOf('=')+1);
        Double min = Double.parseDouble(minAndMax.split(",")[0]);
        Double max = Double.parseDouble(minAndMax.split(",")[1]);
        getMin().set(min);
        getMax().set(max);
    }

    private String getPixelTypeValue(String line) {
        return line.substring(line.indexOf('=')+1);
    }

    @Override
    public void rollback() {
        // nothing to do

    }

    @Override
    public void finalise() {
        // nothing to do

    }

    public Path getSource() {
        return sourceDir;
    }

    public void setSource(Path sourceDir) {
        this.sourceDir = sourceDir;
    }

    public ScalarReceiver<Double> getMax() {
        if (max == null)
            max = new ScalarReceiver<Double>();
        return max;
    }

    public void setMax(ScalarReceiver<Double> max) {
        this.max = max;
    }

    public ScalarReceiver<Double> getMin() {
        if (min == null)
            min = new ScalarReceiver<Double>();
        return min;
    }

    public void setMin(ScalarReceiver<Double> min) {
        this.min = min;
    }

    public ScalarReceiver<Double> getNodata() {
        if (nodata == null)
            nodata = new ScalarReceiver<Double>();
        return nodata;
    }

    public void setNodata(ScalarReceiver<Double> nodata) {
        this.nodata = nodata;
    }

    public ScalarReceiver<String> getPixelType() {
        if (pixelType == null)
            pixelType = new ScalarReceiver<String>();
        return pixelType;
    }

    public void setPixelType(ScalarReceiver<String> pixelType) {
        this.pixelType = pixelType;
    }
}
