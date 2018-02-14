package org.vpac.ndg.task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vpac.ndg.ApplicationContextProvider;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.colour.NamedPalette;
import org.vpac.ndg.colour.Palette;
import org.vpac.ndg.common.datamodel.GdalFormat;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.configuration.NdgConfig;
import org.vpac.ndg.configuration.NdgConfig.PreviewSpec;
import org.vpac.ndg.configuration.NdgConfigManager;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.geometry.Box;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * WMTS Query Creator uses a number of gdal commands to build a set of tiles suitable
 * for consumption by a Web Mapping Tiling Service (WMTS) client
 *
 * @author lachlan
 *
 */
public class WmtsQueryCreator extends Application {

    public static final String WMTS_TILE_DIR = "wmts";

    final private Logger log = LoggerFactory.getLogger(WmtsQueryCreator.class);

    private String queryJobProgressId;

    // Legacy; replaced by palette
    private boolean continuous = true;
    private String palette;
    private Palette _palette;

    JobProgressDao jobProgressDao;
    NdgConfigManager ndgConfigManager;

    DatasetUtil datasetUtil;

    public WmtsQueryCreator() {
        ApplicationContext appContext = ApplicationContextProvider.getApplicationContext();
        jobProgressDao = (JobProgressDao) appContext.getBean("jobProgressDao");
        ndgConfigManager = (NdgConfigManager) appContext.getBean("ndgConfigManager");

        datasetUtil = new DatasetUtil();
    }


    @Override
    protected void initialise() throws TaskInitialisationException {
        super.initialise();

        log.info("Initialising WMTS tile creation for  query with task/job id {}", queryJobProgressId);

        if (queryJobProgressId == null) {
            throw new TaskInitialisationException("No query job/task id specified.");
        }

        JobProgress jobProgress = jobProgressDao.retrieve(queryJobProgressId);
        if(jobProgress == null) {
            // Capture if dataset not exist
            throw new TaskInitialisationException(String.format("Job/Task with ID = \"%s\" not found.", queryJobProgressId));
        }

        // Get a palette.
        // Currently, the value range is always scaled to be between 1 and 255
        // before colours are fetched from the palette (see Task 4,
        // `Translator vrtToByteTif` below).
        if (palette == null) {
            // Use deprecated palettes; this is for backwards compatibility
            // with old clients that don't know how to specify a palette.
            if (continuous)
                _palette = NamedPalette.get("rainbow360", 1, 255);
            else
                _palette = NamedPalette.get("cyclic11", 1, 255);
        } else {
            _palette = NamedPalette.get(palette, 1, 255);
        }

        log.info("Query Job Progress : {}", jobProgress);

        if (jobProgress.getState() == TaskState.FINISHED && jobProgress.getTaskType() == TaskType.Query) {
            //then the task does refer to a query, that has completed execution
        }
        else {
            //then there's not much we can do. Task either doesn't refer to a query, or query failed/hasn't yet
            //completed.
            throw new TaskInitialisationException(String.format("Job/Task with ID = \"%s\" does not refer to a query and/or has not completed execution.", queryJobProgressId));
        }

		NdgConfig config = ndgConfigManager.getConfig();
		if (config.getPreview().getBaseResolution() == 0.0) {
			throw new TaskInitialisationException(
					"No map resolution has been defined.");
		}
		if (config.getPreview().getExtents().getHeight() <= 0 ||
				config.getPreview().getExtents().getWidth() <= 0) {
			throw new TaskInitialisationException("Map extents have no area.");
		}
    }


    /**
     * make the tasks that will run the tile creation process.
     * @throws TaskException
     */
    protected void createTasks() throws TaskInitialisationException
    {

        if (queryJobProgressId == null) {
            throw new TaskInitialisationException("Query job/task id not specified");
        }

        createTasksForQuery(this.queryJobProgressId);
    }

    protected void createTasksForQuery(String queryJobProgressId) throws TaskInitialisationException
    {


        //
        // TASK 2
        // Make a VRT file containing all the timeslices netcdf files for a single band
        // gdalbuildvrt -te 1786000 1997265 2999979.288 2900000.000 -tr 66.146 66.146 -srcnodata 0 -a_srs EPSG:3111 -overwrite lga_extents.vrt *.nc
        //
        String targetName = String.format("%s", queryJobProgressId);
        Path vrtMosaicLoc = getWorkingDirectory().resolve(targetName + "_scaled" + Constant.EXT_VRT);
        GraphicsFile vrtMosaicFile = new GraphicsFile(vrtMosaicLoc);
        // Bug fix for vrt builder when one of the output files's datatype is
        // byte then vrt cannot convert it properly. So forcely set -1 to 255
        OutputDirStatistics outputInfo = new OutputDirStatistics();
        List<Path> sampleFiles = getSourceFilesFromPath(getQueryResultsDirectory());
        if (sampleFiles.size() > 0) {
            outputInfo.setSource(getQueryResultsDirectory());
        }

        GraphicsFile allQueryTiles = new GraphicsFile(getQueryResultsDirectory());
        List<GraphicsFile> translateInputs = new ArrayList<>();
        VrtBuilder sourceVrtBuilder = new VrtBuilder("Create VRT of single band/timeslice");
        setTaskCleanupOptions(sourceVrtBuilder);
        sourceVrtBuilder.setSource(allQueryTiles);
        sourceVrtBuilder.setTemporaryLocation(getWorkingDirectory());
        sourceVrtBuilder.setOutputBucket(translateInputs);
        sourceVrtBuilder.setCopyToStoragePool(false);

        //now the WMTS specifics
		PreviewSpec preview = ndgConfigManager.getConfig().getPreview();
		Box extents = preview.getExtents();
		sourceVrtBuilder.setTargetResolution(
				preview.getBaseResolution(), preview.getBaseResolution());
		sourceVrtBuilder.setTargetExtents(
				extents.getXMin(), extents.getYMin(),
				extents.getXMax(), extents.getYMax());

        ScalarReceiver<OutputDirStatistics> output = new ScalarReceiver<OutputDirStatistics>();
        output.set(outputInfo);
        sourceVrtBuilder.setOutputDirStatistics(output);
        sourceVrtBuilder.setTarget(vrtMosaicFile);
        //nodata value is set based on the bands nodata

        //
        // TASK 3
        // Get the min and max for the dataset so we can scale this into a range of 0-255 appropriately
        //
        //FileStatistics stats = new FileStatistics();
        //stats.setProgressWeight(5.0);
        //stats.setSource(vrtMosaicFile);
        //stats.setApproximate(false);
        //NOTE: we grab some of the scalar recievers from this task to feed values into the next task

        //
        // TASK 4
        // Convert vrt file into a byte based geotiff, gdal was having problems with too many nested vrts
        // hence this step was required
        Path vrtScaledByte = getWorkingDirectory().resolve(targetName + "_scaled_byte" + GdalFormat.VRT.getExtension());
        GraphicsFile vrtScaledByteFile = new GraphicsFile(vrtScaledByte);
        vrtScaledByteFile.setFormat(GdalFormat.VRT);

        Translator vrtToByteScaledVrt = new Translator("Translating vrt (nc based) into tif file of type byte");
        vrtToByteScaledVrt.setProgressWeight(5.0);
        setTaskCleanupOptions(vrtToByteScaledVrt);
        vrtToByteScaledVrt.setSource(vrtMosaicFile);
        vrtToByteScaledVrt.setTarget(vrtScaledByteFile);
        vrtToByteScaledVrt.setOutputType("Byte");
        vrtToByteScaledVrt.setNodata("0");
        ScalarReceiver<Double> byteLowestValue = new ScalarReceiver<Double>();
        byteLowestValue.set(1.0);
        ScalarReceiver<Double> byteHighestValue = new ScalarReceiver<Double>();
        byteHighestValue.set(255.0);

        //set the scale using scalar recievers as this info will not be know till the previous task is run
        List<ScalarReceiver<Double>> scale = new ArrayList<ScalarReceiver<Double>>();
        scale.add(output.get().getMin());
        scale.add(output.get().getMax());
        scale.add(byteLowestValue);
        scale.add(byteHighestValue);
        vrtToByteScaledVrt.setScale(scale);

        //
        // TASK 6
        // Make a VRT file with colour table
        Path vrtWithColour = getWorkingDirectory().resolve(targetName + "_scaled_byte_colour" + Constant.EXT_VRT);
        GraphicsFile vrtWithColourFile = new GraphicsFile(vrtWithColour);

        VrtColouriser vrtColourer = new VrtColouriser();
        vrtColourer.setInsertBefore("<ComplexSource>");
        vrtColourer.setProgressWeight(12.0);
        setTaskCleanupOptions(vrtColourer);
        vrtColourer.setSource(vrtScaledByteFile);
        vrtColourer.setTarget(vrtWithColourFile);
        log.info("TASK 6, vrtWithColourFile:" + vrtWithColourFile);
        log.info("TASK 6, palette:" + _palette);
        vrtColourer.setPalette(_palette);

        //
        // TASK 7
        // Make a VRT with an expanded colour 'thing'. gdal2tiles requires this step fortunately it's quick
        Path vrtWithColourExpanded = getWorkingDirectory().resolve(targetName + "_scaled_byte_colour_expanded" + Constant.EXT_VRT);
        GraphicsFile vrtWithColourExpandedFile = new GraphicsFile(vrtWithColourExpanded);
        vrtWithColourExpandedFile.setFormat(GdalFormat.VRT);

        Translator vrtToExpandedVrt = new Translator("Expanding vrt with colour table");
        vrtToExpandedVrt.setProgressWeight(20.0);
        setTaskCleanupOptions(vrtToExpandedVrt);
        vrtToExpandedVrt.setSource(vrtWithColourFile);
        vrtToExpandedVrt.setTarget(vrtWithColourExpandedFile);
        log.info("TASK 7, vrtWithColourFile:" + vrtWithColourFile);
        vrtToExpandedVrt.setExpand("rgba");

        //
        // TASK 8
        // Use gdal2tiles.py to actually build the tiles (HORAy)
        Path wmtsDir = getWorkingDirectory().resolve(targetName);

        TileBuilder tileBuilder = new TileBuilder();
        tileBuilder.setProgressWeight(70.0);
        tileBuilder.setSource(vrtWithColourFile);
        tileBuilder.setTarget(wmtsDir);
        tileBuilder.setProfile("raster");


        // ADD TASKS
        getTaskPipeline().addTask(outputInfo);
        getTaskPipeline().addTask(sourceVrtBuilder);
        //getTaskPipeline().addTask(stats);
        getTaskPipeline().addTask(vrtToByteScaledVrt);
        getTaskPipeline().addTask(vrtColourer);
        getTaskPipeline().addTask(vrtToExpandedVrt);
        getTaskPipeline().addTask(tileBuilder);

    }

    private List<Path> getSourceFilesFromPath(Path dir) {
        List<Path> sourceFiles = new ArrayList<Path>();
        if (dir.toFile().isDirectory())
        {
            File[] filesInDir = dir.toFile().listFiles();
            for (File fileInDir : filesInDir) {
                if (fileInDir.isFile()) {
                    sourceFiles.add(fileInDir.toPath());
                }
            }
        } else {
            sourceFiles.add(dir.toFile().toPath());
        }
        return sourceFiles;
    }


    /**
     * stop the task pipeline from deleting the output files automatically
     * @param t
     */
    private void setTaskCleanupOptions(BaseTask t) {
        t.setCleanupSource(false);
        t.setCleanupTarget(false);
    }

    /**
     * returns the directory where the results for this query were placed
     * @return
     */
    protected Path getQueryResultsDirectory() {
        Path pickupPath = Paths.get(ndgConfigManager.getConfig().getDefaultPickupLocation());
        Path queryPickupPath = pickupPath.resolve(getQueryJobProgressId());

        return queryPickupPath;
    }

    /**
     * working directory for the WMTS Query creator is currently under the queries pickup dir
     */
    protected Path getWorkingDirectory() {
        Path queryPickupPath = getQueryResultsDirectory();

        Path wd = queryPickupPath.resolve(WMTS_TILE_DIR);
        if (!wd.toFile().exists())
            wd.toFile().mkdirs();
        return wd;
    }

    @Override
    protected String getJobName() {
        return "building wmts tiles";
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.WmtsTileBuilding;
    }

    @Override
    protected String getJobDescription() {
        if (this.queryJobProgressId != null)
            return String.format("Builing WMTS tiles for query %s", queryJobProgressId);
        else
            return "Builing WMTS query tiles";
    }

    public String getQueryJobProgressId() {
        return queryJobProgressId;
    }

    public boolean isContinuous() {
        return continuous;
    }

    /**
     * specifies whether this query result is to be rendered using a continuous colour range.
     * @param continuous
     */
    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public String getPalette() {
		return palette;
	}


	public void setPalette(String palette) {
		this.palette = palette;
	}


	/**
     * needs to be the task/job id of a completed query
     * @param jobProgressId
     */
    public void setQueryJobProgressId(String jobProgressId) {
        this.queryJobProgressId = jobProgressId;
    }

    @Override
    protected void preExecute() throws TaskException {

    }
    @Override
    protected void postExecute() {

    }
}
