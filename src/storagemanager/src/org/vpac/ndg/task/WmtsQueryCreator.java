package org.vpac.ndg.task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vpac.ndg.ApplicationContextProvider;
import org.vpac.ndg.Utils;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.GdalFormat;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.configuration.NdgConfigManager;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.geometry.Box;
import org.vpac.ndg.geometry.NestedGrid;
import org.vpac.ndg.geometry.Projection;
import org.vpac.ndg.geometry.TileManager;
import org.vpac.ndg.lock.TimeSliceDbReadWriteLock;
import org.vpac.ndg.storage.dao.DatasetDao;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.TimeSliceDao;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TileBand;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.ndg.storage.util.TimeSliceUtil;
import org.vpac.ndg.storagemanager.GraphicsFile;
import org.vpac.ndg.task.VrtColouriser.ColourTableType;

/**
 * WMTS Query Creator uses a number of gdal commands to build a set of tiles suitable
 * for consumption by a Web Mapping Tiling Service (WMTS) client
 * 
 * @author lachlan
 *
 */
public class WmtsQueryCreator extends Application {

    public static final String WMTS_TILE_DIR = "wmts";
    
    //FIXME: these should not be hardcoded
    //Unfortunately these params are related to what the client requires, and
    //not information available to the server/rsa. The client should be able
    //to provide these details, but then we'd have to run the tile reconstruction
    //process again.
    private double tr[] = {66.146,66.146};
    private double te[] = {1786000, 1997265, 2999979.288, 2900000.000};
    
    
    final private Logger log = LoggerFactory.getLogger(WmtsQueryCreator.class);
    
    private String queryJobProgressId;


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

        log.info("Query Job Progress : {}", jobProgress);
        
        if (jobProgress.getState() == TaskState.FINISHED && jobProgress.getTaskType() == TaskType.Query) {
            //then the task does refer to a query, that has completed execution
        }
        else {
            //then there's not much we can do. Task either doesn't refer to a query, or query failed/hasn't yet
            //completed.
            throw new TaskInitialisationException(String.format("Job/Task with ID = \"%s\" does not refer to a query and/or has not completed execution.", queryJobProgressId));
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
        
        GraphicsFile allQueryTiles = new GraphicsFile(getQueryResultsDirectory());
        
        List<GraphicsFile> translateInputs = new ArrayList<>();
        
        VrtBuilder sourceVrtBuilder = new VrtBuilder("Create VRT of single band/timeslice");
        setTaskCleanupOptions(sourceVrtBuilder);
        sourceVrtBuilder.setSource(allQueryTiles);
        sourceVrtBuilder.setTemporaryLocation(getWorkingDirectory());
        sourceVrtBuilder.setOutputBucket(translateInputs);
        sourceVrtBuilder.setCopyToStoragePool(false);
        sourceVrtBuilder.setTarget(vrtMosaicFile);
        
        //now the WMTS specifics
        sourceVrtBuilder.setTargetResolution(tr[0], tr[1]);
        sourceVrtBuilder.setTargetExtents(te[0], te[1], te[2], te[3]);
        //nodata value is set based on the bands nodata
        
        
        //
        // TASK 3
        // Get the min and max for the dataset so we can scale this into a range of 0-255 appropriately
        //
        FileStatistics stats = new FileStatistics();
        stats.setSource(vrtMosaicFile);
        stats.setApproximate(false);
        //NOTE: we grab some of the scalar recievers from this task to feed values into the next task
        
        //
        // TASK 4
        // Convert vrt file into a byte based geotiff, gdal was having problems with too many nested vrts
        // hence this step was required
        Path tifByte = getWorkingDirectory().resolve(targetName + "_byte" + GdalFormat.GEOTIFF.getExtension());
        GraphicsFile tifByteFile = new GraphicsFile(tifByte);
        tifByteFile.setFormat(GdalFormat.GEOTIFF);
        
        Translator vrtToByteTif = new Translator("Translating vrt (nc based) into tif file of type byte");
        setTaskCleanupOptions(vrtToByteTif);
        vrtToByteTif.setSource(vrtMosaicFile);
        vrtToByteTif.setTarget(tifByteFile);
        vrtToByteTif.setOutputType("Byte");
        ScalarReceiver<Double> byteLowestValue = new ScalarReceiver<Double>();
        byteLowestValue.set(1.0);
        ScalarReceiver<Double> byteHighestValue = new ScalarReceiver<Double>();
        byteHighestValue.set(255.0);
        
        //set the scale using scalar recievers as this info will not be know till the previous task is run
        List<ScalarReceiver<Double>> scale = new ArrayList<ScalarReceiver<Double>>();
        scale.add(stats.getMin());
        scale.add(stats.getMax());
        scale.add(byteLowestValue);
        scale.add(byteHighestValue);
        vrtToByteTif.setScale(scale);
        
        
        //
        // TASK 5
        // Make another vrt file so that we can insert a colour table into it
        Path vrtWithNoColour = getWorkingDirectory().resolve(targetName + "_noColour" + Constant.EXT_VRT);
        GraphicsFile vrtWithNoColourFile = new GraphicsFile(vrtWithNoColour);
        
        VrtBuilder noColourVrtBuilder = new VrtBuilder("Building VRT based on Byte layer");
        setTaskCleanupOptions(noColourVrtBuilder);
        noColourVrtBuilder.setSource(tifByteFile);
        noColourVrtBuilder.setTarget(vrtWithNoColourFile);
        noColourVrtBuilder.setTemporaryLocation(getWorkingDirectory());
        noColourVrtBuilder.setCopyToStoragePool(false);
        
        //
        // TASK 6
        // Make a VRT file with colour table
        Path vrtWithColour = getWorkingDirectory().resolve(targetName + "_Colour" + Constant.EXT_VRT);
        GraphicsFile vrtWithColourFile = new GraphicsFile(vrtWithColour);
        
        VrtColouriser vrtColourer = new VrtColouriser();
        setTaskCleanupOptions(vrtColourer);
        vrtColourer.setSource(vrtWithNoColourFile);
        vrtColourer.setTarget(vrtWithColourFile);
        
        
        if (true) {
            vrtColourer.setColourTableType(ColourTableType.CONTINUOUS);
        } else {
            vrtColourer.setColourTableType(ColourTableType.CATAGORICAL);
        }
        
        //
        // TASK 7
        // Make a VRT with an expanded colour 'thing'. gdal2tiles requires this step fortunately it's quick
        Path vrtWithColourExpanded = getWorkingDirectory().resolve(targetName + "_Colour_Expanded" + Constant.EXT_VRT);
        GraphicsFile vrtWithColourExpandedFile = new GraphicsFile(vrtWithColourExpanded);
        vrtWithColourExpandedFile.setFormat(GdalFormat.VRT);
        
        Translator vrtToExpandedVrt = new Translator("Expanding vrt with colour table");
        setTaskCleanupOptions(vrtToExpandedVrt);
        vrtToExpandedVrt.setSource(vrtWithColourFile);
        vrtToExpandedVrt.setTarget(vrtWithColourExpandedFile);
        vrtToExpandedVrt.setExpand("rgba");
        
        //
        // TASK 8
        // Use gdal2tiles.py to actually build the tiles (HORAy)
        Path wmtsDir = getWorkingDirectory().resolve(targetName);
        
        TileBuilder tileBuilder = new TileBuilder();
        tileBuilder.setSource(vrtWithColourExpandedFile);
        tileBuilder.setTarget(wmtsDir);
        tileBuilder.setProfile("raster");
        
        
        // ADD TASKS
        getTaskPipeline().addTask(sourceVrtBuilder);
        getTaskPipeline().addTask(stats);
        getTaskPipeline().addTask(vrtToByteTif);
        getTaskPipeline().addTask(noColourVrtBuilder);
        getTaskPipeline().addTask(vrtColourer);
        getTaskPipeline().addTask(vrtToExpandedVrt);
        getTaskPipeline().addTask(tileBuilder);
        
    }
    
    /**
     * stop the task pipeline from deleting the output files automatically
     * @param t
     */
    private void setTaskCleanupOptions(Task t) {
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
        boolean createdTempDir = false;
        if (!wd.toFile().exists())
        {
            createdTempDir = wd.toFile().mkdirs();
        }
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
