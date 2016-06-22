package org.vpac.ndg.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class FileStatistics extends Task {

    final private Logger log = LoggerFactory.getLogger(FileStatistics.class);

    //key strings used to find stats in gdalinfo output
    private final String STATS_MAX = "STATISTICS_MAXIMUM";
    private final String STATS_MEAN = "STATISTICS_MEAN";
    private final String STATS_MIN = "STATISTICS_MINIMUM";
    private final String STATS_STDDEV = "STATISTICS_STDDEV";
    private final String COMPUTED_MINMAX = "Computed Min/Max";
    private final String PIXEL_TYPE = "PIXELTYPE";
    private final String NO_DATA = "NoData Value";

    private GraphicsFile source;
    private boolean approximate;

    //the gathered stats
    private ScalarReceiver<Double> max;
    private ScalarReceiver<Double> min;
    private ScalarReceiver<Double> mean;
    private ScalarReceiver<Double> stddev;
    private ScalarReceiver<Double> nodata;
    private ScalarReceiver<String> pixelType;



    public FileStatistics() {
        this("Extracting file statistics");
    }

    public FileStatistics(String description) {
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

        command.add(source.getFileLocation().toString());
        log.info("command:" + command);
        return command;
    }



    @Override
    public void execute(Collection<String> actionLog, IProgressCallback progressCallback) throws TaskException {
        List<String> command = prepareCommand();

        String listString = "";
        for (String s : command) {
            listString += s + " ";
        }
        log.info(listString);

        String stdout = "";
        try {

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


        } catch (IOException e) {
            throw new TaskException(getDescription(), e);
        }

        //We'll end up with a block of data at the end of this similar to
        /* STATISTICS_MAXIMUM=389
           STATISTICS_MEAN=341.93609333722
           STATISTICS_MINIMUM=300
           STATISTICS_STDDEV=23.6484643221
         */

        int foundCount = 0;

        //Read the info we're after and put it in some scalar receivers
        String lines[] = stdout.split(System.lineSeparator());
        for (String line: lines) {
            line = line.trim();
            if (line.startsWith(STATS_MAX)) {
                getMax().set(getStatsValue(line));
                log.info("Found max of " + getMax().get().toString());
                foundCount++;
            } else
            if (line.startsWith(STATS_MEAN)) {
                getMean().set(getStatsValue(line));
                log.info("Found mean of " + getMean().get().toString());
                foundCount++;
            } else if (line.startsWith(STATS_MIN)) {
                getMin().set(getStatsValue(line));
                // getMin().set(Double.parseDouble("0"));
                log.info("Found min of " + getMin().get().toString());
                foundCount++;
            } else if (line.startsWith(STATS_STDDEV)) {
                getStddev().set(getStatsValue(line));
                log.info("Found stddev of " + getStddev().get().toString());
                foundCount++;
            } else if (line.startsWith(PIXEL_TYPE)) {
                getPixelType().set(getPixelTypeValue(line));
                log.info("Found pixel type of " + getPixelType().get());
                foundCount++;
            } else if (line.startsWith(NO_DATA)) {
                getNodata().set(getStatsValue(line));
                log.info("Found No data value of " + getNodata().get());
                foundCount++;

            // } else if (line.startsWith(COMPUTED_MINMAX)) {
            //     setComputedValues(line);
            //     log.info("Computed Min and Max of " + getMin().get().toString()
            //         + "," + getMax().get().toString());
            //     foundCount += 2;
            }
        }

        if (foundCount < 4) {
            StringBuilder sb = new StringBuilder();
            int start = lines.length - 11;
            if (start < 0)
                start = 0;
            for (int i = start; i < lines.length; i++) {
                if (i > start)
                    sb.append("\n");
                sb.append(lines[i]);
            }
            // throw new TaskException(String.format(
            //         "Unable to extract statistics from gdalinfo output."
            //         + "Last 10 lines were\n %s", sb.toString()));
         }
    }

    /**
     * gets the value that comes after the equals sign
     * @param line
     * @return
     */
    private double getStatsValue(String line) {
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

    public GraphicsFile getSource() {
        return source;
    }

    public void setSource(GraphicsFile source) {
        this.source = source;
    }

    public boolean isApproximate() {
        return approximate;
    }

    /**
     * gather approximate stats only. testing shows this is significantly quicker (about 10x quicker)
     * @param approximate
     */
    public void setApproximate(boolean approximate) {
        this.approximate = approximate;
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

    public ScalarReceiver<Double> getMean() {
        if (mean == null)
            mean = new ScalarReceiver<Double>();
        return mean;
    }

    public void setMean(ScalarReceiver<Double> mean) {
        this.mean = mean;
    }

    public ScalarReceiver<Double> getStddev() {
        if (stddev == null)
            stddev = new ScalarReceiver<Double>();
        return stddev;
    }

    public void setStddev(ScalarReceiver<Double> stddev) {
        this.stddev = stddev;
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
        log.info("getPixelType:" + pixelType.get());
        return pixelType;
    }

    public void setPixelType(ScalarReceiver<String> pixelType) {
        this.pixelType = pixelType;
    }
}
