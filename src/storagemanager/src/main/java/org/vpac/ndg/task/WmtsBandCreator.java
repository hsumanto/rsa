package org.vpac.ndg.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vpac.ndg.ApplicationContextProvider;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.colour.NamedPalette;
import org.vpac.ndg.colour.Palette;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.GdalFormat;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.configuration.NdgConfig;
import org.vpac.ndg.configuration.NdgConfigManager;
import org.vpac.ndg.configuration.NdgConfig.PreviewSpec;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.geometry.Box;
import org.vpac.ndg.geometry.NestedGrid;
import org.vpac.ndg.geometry.TileManager;
import org.vpac.ndg.lock.TimeSliceDbReadWriteLock;
import org.vpac.ndg.storage.dao.BandDao;
import org.vpac.ndg.storage.dao.DatasetDao;
import org.vpac.ndg.storage.dao.TimeSliceDao;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.TileBand;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.ndg.storage.util.TimeSliceUtil;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * WMTS Band Creator uses a number of gdal commands to build a set of tiles suitable
 * for consumption by a Web Mapping Tiling Service (WMTS) client
 *
 * @author lachlan
 *
 */
public class WmtsBandCreator extends Application {

    public static final String WMTS_TILE_DIR = "wmts";

    final private Logger log = LoggerFactory.getLogger(WmtsBandCreator.class);

    private String datasetId;
    private String bandId;
    private String timesliceId;

    private Dataset dataset;

    private String palette;
    private Palette _palette;

    private Box internalExtents;

    // It's OK to hold a direct reference to the time slices here, because this
    // application doesn't modify them.
    private List<TimeSlice> timeSlices;

    private TimeSliceDbReadWriteLock lock;

    DatasetDao datasetDao;
    BandDao bandDao;
    TimeSliceDao timeSliceDao;
    TimeSliceUtil timeSliceUtil;
    TileManager tileManager;
    NdgConfigManager ndgConfigManager;

    DatasetUtil datasetUtil;


    public WmtsBandCreator() {
        ApplicationContext appContext = ApplicationContextProvider.getApplicationContext();
        datasetDao = (DatasetDao) appContext.getBean("datasetDao");
        bandDao = (BandDao) appContext.getBean("bandDao");
        timeSliceDao = (TimeSliceDao) appContext.getBean("timeSliceDao");
        timeSliceUtil = (TimeSliceUtil) appContext.getBean("timeSliceUtil");
        tileManager = (TileManager) appContext.getBean("tileManager");
        ndgConfigManager = (NdgConfigManager) appContext.getBean("ndgConfigManager");

        datasetUtil = new DatasetUtil();
    }


    @Override
    protected void initialise() throws TaskInitialisationException {
        super.initialise();

        log.info("Initialising WMTS tile creation for  dataset {} , band {}", datasetId, bandId);

        if (datasetId == null) {
            throw new TaskInitialisationException("No dataset specified.");
        }

        dataset = datasetDao.retrieve(datasetId);
        if(dataset == null) {
            // Capture if dataset not exist
            throw new TaskInitialisationException(String.format("Dataset with ID = \"%s\" not found.", datasetId));
        }

        log.info("Dataset: {}", dataset);

        //use the timeslice id if provided, otherwise run over all of them
        if (timesliceId == null) {
            timeSlices = datasetDao.getTimeSlices(datasetId);
        } else {
            TimeSlice ts = timeSliceDao.retrieve(timesliceId);
            if (ts == null) {
                throw new TaskInitialisationException(String.format("Timeslice with ID = \"%s\" not found.", timesliceId));
            }
            List<TimeSlice> tss = new ArrayList<TimeSlice>();
            tss.add(ts);
            timeSlices = tss;
        }

        Collections.sort(timeSlices);
        if (timeSlices.size() == 0)
            throw new TaskInitialisationException("No time slices to tile.");


        // Get the internal extents, we need these to get the timeslices with later
        CellSize resolution = dataset.getResolution();
        NestedGrid nng = tileManager.getNngGrid();
        internalExtents = timeSliceUtil.aggregateBounds(timeSlices);
        if (internalExtents == null) {
            throw new TaskInitialisationException(
                    "None of the selected time slices contain any data, " +
                    "therefore the bounds could not be inferred.");
        }
        internalExtents = nng.alignToGrid(internalExtents, resolution);

        // Get a palette.
        // Currently, the value range is always scaled to be between 1 and 255
        // before colours are fetched from the palette (see Task 4,
        // `Translator vrtToByteTif` below).
        if (palette == null) {
            // Use deprecated palettes; this is for backwards compatibility
            // with old clients that don't know how to specify a palette.
            Band band = bandDao.retrieve(bandId);
            if (band.isContinuous())
                _palette = NamedPalette.get("rainbow360", 1, 255);
            else
                _palette = NamedPalette.get("cyclic11", 1, 255);
        } else {
            _palette = NamedPalette.get(palette, 1, 255);
        }

        // Get locks for all timeslices.
        // FIXME: This should happen before finding timeslice bounds.
        lock = new TimeSliceDbReadWriteLock(TaskType.Export.toString());
        for (TimeSlice ts : timeSlices)
            lock.getTimeSliceIds().add(ts.getId());

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

        if (bandId == null) {
            throw new TaskInitialisationException("Bands not specified");
        }

        List<String> bandIds = new ArrayList<>();
        bandIds.add(bandId);
        List<Band> selectedBands = datasetDao.findBands(datasetId, bandIds);
        if (selectedBands.size() == 0) {
            throw new TaskInitialisationException("No bands with ID " + bandId);
        }
        Band band = selectedBands.get(0);

        for (TimeSlice ts : timeSlices) {
            //add tasks for each timeslice
            createTasksForTimeslice(band,ts);
        }



    }

    protected void createTasksForTimeslice(Band b, TimeSlice ts) throws TaskInitialisationException
    {
        //
        // TASK 1
        // get the tile bands
        List<TileBand> unfilteredtilebands = new ArrayList<TileBand>();
        TileBandCreator tilebandCreator = new TileBandCreator();
        setTaskCleanupOptions(tilebandCreator);
        tilebandCreator.setSource(internalExtents);
        tilebandCreator.setTimeSlice(ts);
        tilebandCreator.setBand(b);
        tilebandCreator.setTarget(unfilteredtilebands);

        //
        // TASK 2
        // Make a VRT file containing all the timeslices netcdf files for a single band
        // gdalbuildvrt -te 1786000 1997265 2999979.288 2900000.000 -tr 66.146 66.146 -srcnodata 0 -a_srs EPSG:3111 -overwrite lga_extents.vrt *.nc
        //
        String targetName = String.format("%s/%s", ts.getId(), b.getId());
        Path vrtMosaicLoc = getWorkingDirectory().resolve(targetName + "_scaled" + Constant.EXT_VRT);
        GraphicsFile vrtMosaicFile = new GraphicsFile(vrtMosaicLoc);

        List<GraphicsFile> translateInputs = new ArrayList<>();

        VrtBuilder sourceVrtBuilder = new VrtBuilder("Create VRT of single band/timeslice");
        setTaskCleanupOptions(sourceVrtBuilder);
        sourceVrtBuilder.setSource(unfilteredtilebands);
        sourceVrtBuilder.setBand(b);
        sourceVrtBuilder.setTimeSlice(ts);
        sourceVrtBuilder.setTemporaryLocation(getWorkingDirectory());
        sourceVrtBuilder.setOutputBucket(translateInputs);
        sourceVrtBuilder.setCopyToStoragePool(false);
        sourceVrtBuilder.setTarget(vrtMosaicFile);

        //now the WMTS specifics
		PreviewSpec preview = ndgConfigManager.getConfig().getPreview();
		Box extents = preview.getExtents();
		sourceVrtBuilder.setTargetResolution(
				preview.getBaseResolution(), preview.getBaseResolution());
		sourceVrtBuilder.setTargetExtents(
				extents.getXMin(), extents.getYMin(),
				extents.getXMax(), extents.getYMax());
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
        scale.add(stats.getMin());
        scale.add(stats.getMax());
        scale.add(byteLowestValue);
        scale.add(byteHighestValue);
        vrtToByteScaledVrt.setScale(scale);


        //
        // TASK 5
        // Make another vrt file so that we can insert a colour table into it
        Path vrtWithNoColour = getWorkingDirectory().resolve(targetName + "_noColour" + Constant.EXT_VRT);
        GraphicsFile vrtWithNoColourFile = new GraphicsFile(vrtWithNoColour);

        VrtBuilder noColourVrtBuilder = new VrtBuilder("Building VRT based on Byte layer");
        setTaskCleanupOptions(noColourVrtBuilder);
        noColourVrtBuilder.setSource(vrtScaledByteFile);
        noColourVrtBuilder.setTarget(vrtWithNoColourFile);
        noColourVrtBuilder.setTemporaryLocation(getWorkingDirectory());
        noColourVrtBuilder.setCopyToStoragePool(false);

        //
        // TASK 6
        // Make a VRT file with colour table
        Path vrtWithColour = getWorkingDirectory().resolve(targetName + "_Colour" + Constant.EXT_VRT);
        GraphicsFile vrtWithColourFile = new GraphicsFile(vrtWithColour);

        VrtColouriser vrtColourer = new VrtColouriser();
        vrtColourer.setInsertBefore("<ComplexSource>");
        setTaskCleanupOptions(vrtColourer);
        vrtColourer.setSource(vrtWithNoColourFile);
        vrtColourer.setTarget(vrtWithColourFile);
        vrtColourer.setPalette(_palette);

        //
        // TASK 8
        // Use gdal2tiles.py to actually build the tiles (HORAy)
        Path wmtsDir = getWorkingDirectory().resolve(targetName);

        TileBuilder tileBuilder = new TileBuilder();
        tileBuilder.setGdal2TilesCommand("gdal2tiles.rsa.py");
        tileBuilder.setSource(vrtWithColourFile);
        tileBuilder.setTarget(wmtsDir);
        tileBuilder.setProfile("raster");


        // ADD TASKS
        getTaskPipeline().addTask(tilebandCreator);
        getTaskPipeline().addTask(sourceVrtBuilder);
        getTaskPipeline().addTask(stats);
        getTaskPipeline().addTask(vrtToByteScaledVrt);
        getTaskPipeline().addTask(noColourVrtBuilder);
        getTaskPipeline().addTask(vrtColourer);
        getTaskPipeline().addTask(tileBuilder);

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
     * working directory for the WMTS Band creator is currently under the dataset dir
     */
    protected Path getWorkingDirectory() {
        Path p = datasetUtil.getPath(dataset);
        Path wd = p.resolve(WMTS_TILE_DIR);
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
        if (dataset == null || bandId == null)
            return String.format("Builing WMTS tiles for dataset %s and band %s", datasetId, bandId);
        else
            return "Builing WMTS tiles";
    }

    public String getBandId() {
        return bandId;
    }

    public void setBandId(String bandId) {
        this.bandId = bandId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }


    public String getTimesliceId() {
        return timesliceId;
    }

    /**
     * setting the timeslice id is optional. If not set the tiles will be created for all
     * timeslices.
     * @param timesliceId
     */
    public void setTimesliceId(String timesliceId) {
        this.timesliceId = timesliceId;
    }


    public String getPalette() {
		return palette;
	}


	public void setPalette(String palette) {
		this.palette = palette;
	}


	@Override
    protected void preExecute() throws TaskException {
        if (!ndgConfigManager.getConfig().isFilelockingOn()) {
            log.debug("Executing pipeline without locking.");
            return;
        }

        if (!lock.readLock().tryLock()) {
            // Unable to get write lock for the timeslice as
            // other task is currently modifying the same timeslice
            throw new TaskException("Unable to perform task as the timeslice is currently locked by other task.");
        }
    }
    @Override
    protected void postExecute() {
        log.debug("Finished executing task pipeline.");
        if (!ndgConfigManager.getConfig().isFilelockingOn()) {
            return;
        }

        lock.readLock().unlock();
    }
}
