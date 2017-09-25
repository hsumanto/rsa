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

package org.vpac.web.controller;

import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.Member;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.vpac.actor.ActorCreator;
import org.vpac.ndg.CommandUtil;
import org.vpac.ndg.FileUtils;
import org.vpac.ndg.Utils;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.Format;
import org.vpac.ndg.common.datamodel.GdalFormat;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.datamodel.AggregationDefinition;
import org.vpac.ndg.datamodel.AggregationOpener;
import org.vpac.ndg.datamodel.RsaAggregationFactory;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.geometry.Box;
import org.vpac.ndg.geometry.Tile;
import org.vpac.ndg.geometry.TileManager;
import org.vpac.ndg.lock.ProcessUpdateTimer;
import org.vpac.ndg.query.Query;
import org.vpac.ndg.query.QueryDefinition.DatasetInputDefinition;
import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.Resolve;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.query.math.ScalarElement;
import org.vpac.ndg.query.math.Type;
import org.vpac.ndg.query.sampling.ArrayAdapter;
import org.vpac.ndg.query.sampling.ArrayAdapterImpl;
import org.vpac.ndg.query.sampling.NodataStrategy;
import org.vpac.ndg.query.sampling.NodataStrategyFactory;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.rasterdetails.RasterDetails;
import org.vpac.ndg.storage.dao.BandDao;
import org.vpac.ndg.storage.dao.DatasetDao;
import org.vpac.ndg.storage.dao.TimeSliceDao;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.dao.UploadDao;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskLedger;
import org.vpac.ndg.storage.model.TimeSlice;
import org.vpac.ndg.storage.model.Upload;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.ndg.storage.util.TimeSliceUtil;
import org.vpac.ndg.storage.util.UploadUtil;
import org.vpac.ndg.task.Exporter;
import org.vpac.ndg.task.ImageTranslator;
import org.vpac.ndg.task.Importer;
import org.vpac.ndg.task.S3Importer;
import org.vpac.ndg.task.WmtsBandCreator;
import org.vpac.ndg.task.WmtsQueryCreator;
import org.vpac.web.exception.ResourceNotFoundException;
import org.vpac.web.model.TableBuilder;
import org.vpac.web.model.request.DataExportRequest;
import org.vpac.web.model.request.FileRequest;
import org.vpac.web.model.request.PagingRequest;
import org.vpac.web.model.request.S3ImportRequest;
import org.vpac.web.model.request.TaskSearchRequest;
import org.vpac.web.model.response.CleanUpResponse;
import org.vpac.web.model.response.DatasetPlotResponse;
import org.vpac.web.model.response.ExportResponse;
import org.vpac.web.model.response.FileInfoResponse;
import org.vpac.web.model.response.ImportResponse;
import org.vpac.web.model.response.QueryResponse;
import org.vpac.web.model.response.TabularResponse;
import org.vpac.web.model.response.TaskCollectionResponse;
import org.vpac.web.model.response.TaskResponse;
import org.vpac.web.util.ControllerHelper;
import org.vpac.web.util.Pager;
import org.vpac.web.util.QueryMutator;
import org.vpac.web.util.QueryPreviewHelper;
import scala.collection.Iterator;
import scala.collection.immutable.SortedSet;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

@Controller
@RequestMapping("/Data")
public class DataController {

	final private Logger log = LoggerFactory.getLogger(DataController.class);

	@Autowired
	private ControllerHelper helper;
	@Autowired
	UploadDao uploadDao;
	@Autowired
	DatasetDao datasetDao;
	@Autowired
	DatasetUtil datasetUtil;
	@Autowired
	UploadUtil uploadUtil;
	@Autowired
	JobProgressDao jobProgressDao;
	@Autowired
	StatisticsDao statisticsDao;
	@Autowired
	TimeSliceDao timeSliceDao;
	@Autowired
	TimeSliceUtil timeSliceUtil;
	@Autowired
	TileManager tileManager;
	@Autowired
	BandDao bandDao;

	private Pager<JobProgress> pager = new Pager<JobProgress>();

	@InitBinder
	public void binder(WebDataBinder binder) {
		helper.BindDateTimeFormatter(binder);
		helper.BindPointFormatter(binder);
	}

	@RequestMapping(value = "/Form", method = RequestMethod.GET)
	public String displayForm() {
		return "FileUploadForm";
	}

	@RequestMapping(value = "/Import/Form", method = RequestMethod.GET)
	public String displayImportForm() {
		return "ImportForm";
	}

	@RequestMapping(value = "/Export/Form", method = RequestMethod.GET)
	public String displayExportForm() {
		return "ExportForm";
	}

	@RequestMapping(value="/Upload", method = RequestMethod.POST)
	//public String uploadFile(@RequestParam String timeSliceId, @RequestParam MultipartFile file, @RequestParam(required = false) String fileId, @RequestParam(required = false) String count, ModelMap model) throws IOException {
	public String uploadFile(@Valid FileRequest fileRequest, ModelMap model) throws Exception {

		log.info("File Upload");
		log.debug("Timeslice ID: {}", fileRequest.getTimeSliceId());
		log.debug("Task ID: {}", fileRequest.getTaskId());

		Upload upload;
		if(fileRequest.getTaskId() == null || fileRequest.getTaskId().isEmpty()) {
			upload = new Upload(fileRequest.getTimeSliceId());
			uploadDao.create(upload);
			log.debug("Create UploadFileId: {}", upload.getFileId());
		}
		else {
			upload = uploadDao.retrieve(fileRequest.getTaskId());
			if(upload == null) {
				// Capture if band not exist
				throw new ResourceNotFoundException(String.format("Upload with task ID = \"%s\" not found.", fileRequest.getTaskId()));
			}
			log.debug("Retrieve UploadFileId: {}", upload.getFileId());
		}
		Path fileIdDir = uploadUtil.getDirectory(upload).toAbsolutePath();
		uploadUtil.createDirectory(upload);

		for (MultipartFile file : fileRequest.getFiles()) {
			Path target = fileIdDir.resolve(file.getOriginalFilename());
			if (!file.isEmpty())
				file.transferTo(target.toFile());
		}

		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new FileInfoResponse(upload.getFileId()));
		return "FileUploadSuccess";
	}

 	@RequestMapping(value="/Import", method = RequestMethod.POST)
	public String importTimeSlice(@RequestParam(required=true) String taskId, @RequestParam(required=true) String bandId, @RequestParam(required=true) String srcnodata, @RequestParam(required=false) Boolean useBilinearInterpolation, ModelMap model ) throws TaskInitialisationException {

		log.info("TimeSlice Import");
		log.debug("Task ID: {}", taskId);
		log.debug("Band ID: {}", bandId);
		log.debug("srcnodata: {}", srcnodata);
		log.debug("useBilinearInterpolation: {}", useBilinearInterpolation);

		Importer importer = new Importer();
		// mandatory
		importer.setUploadId(taskId);
		importer.setBand(bandId);
		importer.setSrcnodata(srcnodata);
		if (useBilinearInterpolation != null)
			importer.setUseBilinearInterpolation(useBilinearInterpolation);

		// After calling runInBackground, we can't access the exporter any more.
		// So the model must be updated after configuration, but before the task
		// is started.
		importer.configure();
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new ImportResponse(importer.getTaskId()));
		importer.runInBackground();

		return "Success";
	}

	@RequestMapping(value="/s3Import", method = RequestMethod.POST)
	public String importS3Data(@Valid @RequestBody S3ImportRequest sir, ModelMap model ) throws TaskInitialisationException {

		// Receive a request to retrieve data from AmazonS3 storage, retrieve data and commit to storage pool
		log.info("s3 Import");
		S3Importer importer = new S3Importer();
		String bucket = sir.getBucket();
		String dsName = sir.getDataset();
		CellSize dsResolution = CellSize.valueOf(sir.getResolution());
		long dsPrecision = Utils.parseTemporalPrecision(sir.getPrecision());

		String tsName = sir.getTimeslice();
		Date tsDate = Utils.parseDate(tsName);
		ArrayList<Integer> xlims = sir.getXlims();
		ArrayList<Integer> ylims = sir.getYlims();
		Box bounds = new Box(xlims.get(0), ylims.get(0), xlims.get(1), ylims.get(1));

		String bandName = sir.getBand();
		Boolean isContinuous = sir.isContinuous();
		Boolean isMetadata = sir.isMetadata();
		RasterDetails type = RasterDetails.valueOf(sir.getType());
		String noData = sir.getNodata();

		ArrayList<String> files = sir.getFiles();

		// Create database entries before downloading tile(s)
		Dataset ds = datasetDao.findDatasetByName(dsName, dsResolution);
		if (ds == null) {
			log.info("Creating new Dataset");
			ds = new Dataset(dsName, dsResolution, dsPrecision);
			datasetDao.create(ds);
		} else if (ds.getPrecision() != dsPrecision) {
			ds.setPrecision(dsPrecision);
			datasetDao.update(ds);
		}

		// TimeSlice
		TimeSlice ts = datasetDao.findTimeSlice(ds.getId(), tsDate);
		if (ts == null) {
			log.info("Creating new TimeSlice");
			ts = new TimeSlice(tsDate);
			ts.setBounds(bounds);
			datasetDao.addTimeSlice(ds.getId(), ts);
		} else {
			ts.setBounds(bounds);
			timeSliceDao.update(ts);
		}

		// Band
		List<String> bandNames = Arrays.asList(bandName);
		List<Band> bands = datasetDao.findBandsByName(ds.getId(), bandNames);
		if (bands.isEmpty()) {
			log.info("Creating new Band");
			Band band = new Band(bandName, isContinuous, isMetadata);
			band.setType(type);
			band.setNodata(noData);
			datasetDao.addBand(ds.getId(), band);
		} else {
			Band band = bands.get(0);
			band.setContinuous(isContinuous);
			band.setMetadata(isMetadata);
			band.setType(type);
			band.setNodata(noData);
			bandDao.update(band);
		}

		// Import tile(s)
		importer.setBucket(bucket);
		importer.setDatasetName(dsName);
		importer.setResolution(dsResolution);
		importer.setTimeSliceName(tsName);
		importer.setBandName(bandName);
		importer.setS3Targets(files);
		importer.setExtension(sir.getExtension());

		importer.configure();
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new ImportResponse(importer.getTaskId()));
		importer.runInBackground();

		return "Success";
	}

	@RequestMapping(value="/Export", method = RequestMethod.POST)
	public String export(@Valid DataExportRequest request, ModelMap model) throws TaskInitialisationException {

		log.info("TimeSlice Export");
		log.debug("Top left: {}", request.getTopLeft());
		log.debug("Bottom right: {}", request.getBottomRight());
		log.debug("Start date: {}", request.getSearchStartDate());
		log.debug("End date: {}", request.getSearchEndDate());
		log.debug("Projection: {}", request.getProjection());
		log.debug("Export resolution: {}", request.getResolution());
		log.debug("Use Bilinear Interpolation: {}", request.getUseBilinearInterpolation());
		log.debug("Format: {}", request.getFormat());

		CellSize exportResolution = null;
		if (request.getResolution() != null) {
			try {
				exportResolution = CellSize.fromHumanString(request.getResolution());
			} catch (IllegalArgumentException e) {
				log.warn("Invalid export resolution string {}, using dataset resolution for export", request.getResolution());
			}
		}

		//default export format is netCDF
		GdalFormat format = GdalFormat.NC;
		if (request.getFormat() != null) {
			try {
				Format rsaformat = Format.valueOf(request.getFormat());
				format = GdalFormat.valueOf(rsaformat);
			} catch (IllegalArgumentException e) {
				log.warn("Invalid export format string {}, defaulting to NetCDF", request.getFormat());
			}
		}


		Exporter exporter = new Exporter();
		// mandatory
		exporter.setDatasetId(request.getDatasetId());
		// optional
		exporter.setBandIds(request.getBandId());
		exporter.setStart(request.getSearchStartDate());
		exporter.setEnd(request.getSearchEndDate());
		exporter.setTargetProjection(request.getProjection());
		exporter.setTargetResolution(exportResolution);
		exporter.setFormat(format);
		exporter.setUseBilinearInterpolation(request.getUseBilinearInterpolation());
		if(request.getTopLeft() != null && request.getBottomRight() != null) {
			Box b = new Box(request.getTopLeft(), request.getBottomRight());
			exporter.setExtents(b);
		}
		else
			exporter.setExtents(null);

		// After calling runInBackground, we can't access the exporter any more.
		// So the model must be updated after configuration, but before the task
		// is started.
		exporter.configure();
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new ExportResponse(exporter.getTaskId()));
		exporter.runInBackground();

		return "Success";
	}

 	@RequestMapping(value="/Task", method = RequestMethod.GET)
	public String getTasks(@Valid TaskSearchRequest request, @Valid PagingRequest page, ModelMap model ) {

		log.info("Data getTasks");
		log.debug("Task Type being searched: {}", request.getSearchType());
		log.debug("Task State being searched: {}", request.getSearchState());

		List<JobProgress> list = jobProgressDao.search(request.getSearchType(), request.getSearchState(), page.getPage(), page.getPageSize());
		if(list.size() > 0)
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, new TaskCollectionResponse(pager.page(list, page)));
		else
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, new TaskCollectionResponse());
		return "List";
	}

	@RequestMapping(value="/Task/{id}", method = RequestMethod.GET)
	public String getTaskById(@PathVariable String id, ModelMap model ) throws ResourceNotFoundException {

		log.debug("Data getTaskById");
		log.debug("Task ID: {}", id);

		JobProgress j = jobProgressDao.retrieve(id);
		if(j != null)
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, new TaskResponse(j));
		else
			throw new ResourceNotFoundException("This task id not found.");
		return "List";
	}

	@RequestMapping(value="/Task/{taskId}/table/{catType}", method = RequestMethod.GET)
	public String getTableByTaskId(
			@PathVariable String taskId,
			@PathVariable String catType,
			@RequestParam(required = false) List<Double> lower,
			@RequestParam(required = false) List<Double> upper,
			@RequestParam(value="cat", required = false) List<String> categories,
			@RequestParam(required = false) String filter,
			ModelMap model) throws ResourceNotFoundException {

		log.info("Data getTableByTaskId");
		log.debug("Task ID: {}", taskId);

		List<TaskCats> tCats;
		if (catType.equals("value"))
			tCats = statisticsDao.searchCats(taskId, filter);
		else
			tCats = statisticsDao.searchCats(taskId, catType);

		if (tCats.size() == 0) {
			throw new ResourceNotFoundException(
					"No data found for this task ID.");
		}

		TaskCats tCat = tCats.get(0);

		TabularResponse response;
		if (catType.equals("value")) {
			// Viewing intrinsic data; use extrinsic filter.
			List<Integer> values = helper.stringsToInts(categories);
			TableBuilder tb = new TableBuilder();
			response = tb.buildIntrinsic(tCat.getCats(), values,
				tCat.getOutputResolution(), tCat.isCategorical());

		} else {
			// Viewing extrinsic categories; use intrinsic filter.
			List<Double> values = helper.stringsToDoubles(categories);
			TableBuilder tb = new TableBuilder();
			response = tb.buildExtrinsic(tCat.getCats(), lower,
					upper, values, tCat.getOutputResolution(),
					tCat.isCategorical());
		}

		response.setCategorisation(catType);
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, response);

		return "List";
	}

	static final Pattern FILTER_PARAM = Pattern.compile("^filter__(\\d+)__(\\w+)$");

	private Double nullSafeParseDouble(String value) {
		if (value == null || value.equals("null"))
			return null;
		else
			return Double.parseDouble(value);
	}

	@RequestMapping(value="/Task/{taskId}/table", method = RequestMethod.GET)
	public String getLedger(
			@PathVariable String taskId,
			@RequestParam(required = false) List<Integer> columns,
			@RequestParam(required = false) String key,
			@RequestParam MultiValueMap<String, String> params,
			ModelMap model) throws ResourceNotFoundException {

		log.info("Data getLedger");
		log.debug("Task ID: {}", taskId);

		List<TaskLedger> tls = statisticsDao.searchTaskLedger(taskId, key);

		if (tls.size() == 0) {
			throw new ResourceNotFoundException(
					"No ledger found for this task ID.");
		}

		TaskLedger tl = tls.get(0);

		if (columns == null || columns.size() == 0) {
			columns = new ArrayList<>();
			int nColumns = tl.getLedger().getBucketingStrategies().size();
			for (int i = 0; i < nColumns; i++) {
				columns.add(i);
			}
		}

		Map<Integer, Set<Double>> colIds = new HashMap<>();
		Map<Integer, List<Double>> colLowerBounds = new HashMap<>();
		Map<Integer, List<Double>> colUpperBounds = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : params.entrySet()) {
			Matcher matcher = FILTER_PARAM.matcher(entry.getKey());
			if (!matcher.matches())
				continue;
			int colIndex = Integer.parseInt(matcher.group(1));
			String type = matcher.group(2);
			if (type.equals("cat")) {
				Set<Double> collection = new HashSet<>();
				for (String value : entry.getValue())
					collection.add(nullSafeParseDouble(value));
				colIds.put(colIndex, collection);
			} else if (type.equals("lower")) {
				List<Double> collection = new ArrayList<>();
				for (String value : entry.getValue())
					collection.add(nullSafeParseDouble(value));
				colLowerBounds.put(colIndex, collection);
			} else if (type.equals("upper")) {
				List<Double> collection = new ArrayList<>();
				for (String value : entry.getValue())
					collection.add(nullSafeParseDouble(value));
				colUpperBounds.put(colIndex, collection);
			} else {
				continue;
			}
		}

		Ledger unfiltered = tl.getLedger();
		Ledger ledger = unfiltered.copy();
		for (Map.Entry<Integer, Set<Double>> entry : colIds.entrySet()) {
			ledger = ledger.filterRows(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Integer, List<Double>> entry : colLowerBounds.entrySet()) {
			List<Double> lowerBounds = entry.getValue();
			List<Double> upperBounds = colUpperBounds.get(entry.getKey());
			ledger = ledger.filterRows(entry.getKey(), lowerBounds, upperBounds);
		}

		TabularResponse response = new TableBuilder().buildLedger(
			ledger, unfiltered, columns, tl.getOutputResolution());
		response.setCategorisation(tl.getKey());
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, response);

		return "List";
	}

	@RequestMapping(value = "/Download/{taskId}", method = RequestMethod.GET)
	public void downloadFile(@PathVariable("taskId") String taskId, HttpServletResponse response) throws TaskException, FileNotFoundException {
		// get your file as InputStream
		Path p = Exporter.findOutputPath(taskId);
		FileInputStream is = new FileInputStream(p.toFile());
	    try {
			// Send headers, such as file name and length. The length is
			// required for streaming to work properly; see
			// http://stackoverflow.com/questions/10552555/response-flushbuffer-is-not-working
			// Not using response.setContentLength(int), because the file may be
			// larger than 2GB, in which case an int would overflow.
			response.setContentType("application-xdownload");
			response.setHeader("Content-Disposition", "attachment; filename=" + p.getFileName().toString());
			response.setHeader("Content-Length", String.format("%d", Files.size(p)));
			response.flushBuffer();
			// Copy file to response's OutputStream (send it to the client)
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			log.warn("IO error writing file to output stream. User may " +
					"have cancelled.");
		}
	}

	@RequestMapping(value = "/Plot", method = RequestMethod.GET)
	public ModelAndView plot(@RequestParam(required = true) String datasetId,
			@RequestParam(required = false) String bandName,
			@RequestParam(required = true) double x,
			@RequestParam(required = true) double y,
			@RequestParam(required = false) Date start,
			@RequestParam(required = false) Date end,
			@RequestParam(required = false) boolean omitNodata,
			Model model)
			throws IOException, InvalidRangeException, QueryException {
		Dataset ds = null;
		if(datasetId.contains("/")) {
			String[] splittedDatasetId = datasetId.split("/");
			String name = splittedDatasetId[0];
			CellSize resolution = CellSize.fromHumanString(splittedDatasetId[1]);
			ds = datasetDao.findDatasetByName(name, resolution);
		} else {
			ds = datasetDao.retrieve(datasetId);
		}
		if (ds == null)
			return null;

		List<TimeSlice> tsList = datasetDao.findTimeSlices(ds.getId(), start, end);
		if (tsList == null)
			return null;

		List<Band> bands = null;
		if (bandName == null || bandName == "") {
			bands = datasetDao.getBands(ds.getId());
		} else {
			List<String> bandNames = new ArrayList<String>();
			bandNames.add(bandName);
			bands = datasetDao.findBandsByName(ds.getId(), bandNames);
		}
		if (bands == null)
			return null;

		RsaAggregationFactory factory = new RsaAggregationFactory();
		ModelAndView modelNView = new ModelAndView("DisplayPlot");

		try {
			AggregationDefinition def = factory.create(ds, tsList, bands, new Box(x, y, x, y));
			log.info("TS0:" + tsList.get(0).getCreated());
			log.info("TSN:" + tsList.get(tsList.size() - 1).getCreated());
			AggregationOpener opener = new AggregationOpener();
			NetcdfDataset dataset = opener.open(def, "");
			Variable varx = dataset.findVariable("x");
			Variable vary = dataset.findVariable("y");
			int xIndex = getArrayIndex(varx, x);
			int yIndex = getArrayIndex(vary, y);

			int[] origin = new int[] {0, yIndex, xIndex };
			int[] shape = new int[] {tsList.size(), 1, 1 };

			Map<String, List<Pair>> bandPlotPair = new HashMap<String, List<Pair>>();
			Array ar = null;
			NodataStrategyFactory ndsfac = new NodataStrategyFactory();
			for(Band b : bands) {
				Variable var = dataset.findVariable(b.getName());
				Type type = Type.get(var.getDataType(), var.isUnsigned());
				NodataStrategy nds = ndsfac.create(var, type);
				ar = var.read(origin, shape);
				ArrayAdapter arradapt = ArrayAdapterImpl.createAndPromote(ar,
						var.getDataType(), nds);
				List<Pair> plotValues = new ArrayList<Pair>();
				for(int i = 0; i < ar.getIndex().getSize(); i++) {
					ScalarElement elem = arradapt.get(i);
					Date timeval = tsList.get(i).getCreated();
					if (elem.isValid())
						plotValues.add(new Pair(timeval, elem.doubleValue()));
					else if (!omitNodata)
						plotValues.add(new Pair(timeval, null));
				}
				bandPlotPair.put(b.getName(), plotValues);
			}
			modelNView.addObject("bandPlotValues", bandPlotPair);
			modelNView.addObject("datasetName", ds.getName());
			modelNView.addObject("pointX", x);
			modelNView.addObject("pointY", y);

			model.addAttribute(ControllerHelper.RESPONSE_ROOT, new DatasetPlotResponse(tsList, ar));

		} catch (IOException e) {
			log.error(e.getStackTrace().toString());
			throw e;
		}


		return modelNView;
	}

//	private String toCSV(List<TimeSlice> tsList) {
//		StringBuilder builder = new StringBuilder();
//		for(TimeSlice ts : tsList) {
//			if(builder.length() > 0)
//				builder.append(',');
//			builder.append(ts.getCreated());
//		}
//		return builder.toString();
//	}
//
//	private String toCSV(Array ar) {
//		StringBuilder builder = new StringBuilder();
//		long arSize = ar.getIndex().getSize();
//		for(int i = 0; i < arSize; i++) {
//			if(builder.length() > 0)
//				builder.append(',');
//			builder.append(ar.getDouble(i));
//		}
//		return builder.toString();
//	}

	private int getArrayIndex(Variable var, double value) throws IOException {
		int index = -1;
		Array ar = var.read();
		for(int i = 0; i < ar.getIndex().getSize(); i++) {
			if(ar.getDouble(i) <= value)
				index = i;
		}
		return index;
	}


	/*
	@RequestMapping(value = "/Download/{file_name}", method = RequestMethod.GET)
	public void getFile(@PathVariable("file_name") String fileName,
			HttpServletResponse response) {
		try {
			// get your file as InputStream
			File f = new File("c:/temp/" + fileName + ".docx");
			InputStream is = new FileInputStream(f);
			// copy it to response's OutputStream
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}
	}
	*/

	@RequestMapping(value = "/CleanUp", method = RequestMethod.POST)
	public String cleanUp(@RequestParam(required=false) Boolean force, ModelMap model) {
		String process = ProcessUpdateTimer.INSTANCE.acquire();
		try {
			int returnCount = timeSliceUtil.cleanOthers(process, force);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, new CleanUpResponse(returnCount));
			return "Success";
		} finally {
			ProcessUpdateTimer.INSTANCE.release();
		}
	}

	@RequestMapping(value = "/DQuery-test", method = RequestMethod.GET)
	public String distributedQueryTest() throws IllegalAccessException, IOException, QueryException {

		final QueryDefinition qd = QueryDefinition.fromXML(Thread
				.currentThread().getContextClassLoader()
				.getResourceAsStream("test.xml"));

		String threads = "1";
		Double minX = 2125500.0;
		Double minY = 2250100.0;
		Double maxX = 2960500.0;
		Double maxY = 2825000.0;
		String startDate = "";
		String endDate = "";
		String netcdfVersion = null;
		String buckets = null;
		List<String> groupBy = null;
		ModelMap model = new ModelMap();

		return query(qd, threads, minX, minY, maxX, maxY, startDate, endDate,
				netcdfVersion, buckets, groupBy, model);
	}

	@RequestMapping(value = "/Akka-test/{mb}/{times}", method = RequestMethod.GET)
	public String akkaTest(@PathVariable("mb") String mb, @PathVariable("times") String times) throws IllegalAccessException, IOException, QueryException {
		ActorRef frontend = ActorCreator.getFrontend();
	    log.info("path: " + frontend.toString());

		int KB     = 1024;
		int MB     = 1024 * KB;
		int m = Integer.parseInt(mb);
		char[] chars = new char[m * MB];
		// Optional step - unnecessary if you're happy with the array being full of \0
		Arrays.fill(chars, 'f');
		String emptyStrings =new String(chars);


		for(int i = 0; i < Integer.parseInt(times); i ++) {
			frontend.tell(new org.vpac.worker.Job.Work(
					UUID.randomUUID().toString(), emptyStrings, Version.netcdf4_classic, new BoxReal("0 0 0 0"),
					"bb", null), ActorRef.noSender());
		}
		return "Success";
	}

	@RequestMapping(value = "/WmtsDatasetGenerate", method = RequestMethod.POST)
	public String wmtsDatasetGenerate(@RequestParam(required = false) String datasetId,
	                                  @RequestParam(required = false) String timesliceId,
	                                  @RequestParam(required = false) String bandId,
	                                  @RequestParam(required = false) String queryJobProgressId,
	                                  @RequestParam(required = false) Boolean continuous,
	                                  @RequestParam(required = false) String palette,
	                                  ModelMap model) throws TaskInitialisationException, IllegalAccessException {

	    if (queryJobProgressId != null) {
	        //then generate tiles for the query results
	        WmtsQueryCreator bandCreator = new WmtsQueryCreator();
	        bandCreator.setQueryJobProgressId(queryJobProgressId);

			bandCreator.setPalette(palette);
			if (continuous != null)
				bandCreator.setContinuous(continuous);

	        bandCreator.configure();
	        model.addAttribute(ControllerHelper.RESPONSE_ROOT, new ExportResponse(bandCreator.getTaskId()));
	        bandCreator.runInBackground();

	        return "Success";
	    } else if (datasetId != null && bandId != null) {
	        //then generate tiles for something stored in the storage pool
	        //Note: timeslice doesn't really matter, if not specified we build all timeslices
	        WmtsBandCreator bandCreator = new WmtsBandCreator();
	        bandCreator.setDatasetId(datasetId);
	        bandCreator.setTimesliceId(timesliceId);
	        bandCreator.setBandId(bandId);
			bandCreator.setPalette(palette);

	        bandCreator.configure();
	        model.addAttribute(ControllerHelper.RESPONSE_ROOT, new ExportResponse(bandCreator.getTaskId()));
	        bandCreator.runInBackground();

	        return "Success";
	    } else {
	        throw new IllegalAccessException("Failed to specify a query job progress id, or a dataset and band id");
	    }


	}


	@RequestMapping(value = "/Query", method = RequestMethod.POST,
			headers = "content-type=multipart/form-data*")
	public String query(@RequestParam(required = false) MultipartFile file,
			@RequestParam(required = false) String threads,
			@RequestParam(required = false) Double minX,
			@RequestParam(required = false) Double minY,
			@RequestParam(required = false) Double maxX,
			@RequestParam(required = false) Double maxY,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String netcdfVersion,
			@RequestParam(required = false) String buckets,
			@RequestParam(required = false) List<String> groupBy,
			ModelMap model)
			throws IOException, QueryException, IllegalAccessException {
		QueryDefinition qd = QueryDefinition.fromXML(file.getInputStream());
		return query(qd, threads, minX, minY, maxX, maxY, startDate, endDate,
				netcdfVersion, buckets, groupBy, model);
	}

	@RequestMapping(value = "/Query", method = RequestMethod.POST)
	public String query(@RequestParam(required = false) String query,
			@RequestParam(required = false) String threads,
			@RequestParam(required = false) Double minX,
			@RequestParam(required = false) Double minY,
			@RequestParam(required = false) Double maxX,
			@RequestParam(required = false) Double maxY,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String netcdfVersion,
			@RequestParam(required = false) String buckets,
			@RequestParam(required = false) List<String> groupBy,
			ModelMap model)
			throws IOException, QueryException, IllegalAccessException {
		QueryDefinition qd = QueryDefinition.fromString(query);
		return query(qd, threads, minX, minY, maxX, maxY, startDate, endDate,
				netcdfVersion, buckets, groupBy, model);
	}

	static final Pattern GROUP_PATTERN = Pattern.compile("rsa:([^/]+)/([^/]+)/([^/]+)");

	public String query(QueryDefinition qd, String threads,
			Double minX, Double minY, Double maxX, Double maxY,
			String startDate, String endDate, String netcdfVersion,
			String buckets, List<String> groupBy, ModelMap model)
			throws IOException, QueryException, IllegalAccessException {

		Resolve resolve = new Resolve();

		String baseRsaDatasetRef = resolve.decompose(qd.output.grid.ref).getNodeId();
		String baseRsaDatasetName = "";
		CellSize baseRsaDatasetResolution = null;

		DatasetInputDefinition diGrid = null;
		for (DatasetInputDefinition di : qd.inputs) {
			if (di.id.equals(baseRsaDatasetRef)) {
				diGrid = di;
				break;
			}
		}

		// Find extents of grid so that it can be spatially split and
		// distributed across multiple nodes for processing.
		Box extent = null;
		if (diGrid == null)
			throw new IllegalArgumentException("No input reference of output grid");

		if (diGrid.href.startsWith("rsa")) {
			String[] baseRsaDataset = diGrid.href.replace("rsa:", "").split("/");
			baseRsaDatasetName = baseRsaDataset[0];
			baseRsaDatasetResolution = CellSize.fromHumanString(baseRsaDataset[1]);

			Dataset dataset = datasetDao.findDatasetByName(baseRsaDatasetName, baseRsaDatasetResolution);
			if(dataset == null)
				throw new IllegalArgumentException(
					String.format("No dataset found: %s at %s",
					baseRsaDatasetName, baseRsaDatasetResolution));
			String datasetId = dataset.getId();
			List<TimeSlice> tsList = datasetDao.getTimeSlices(datasetId);
			if(tsList == null)
				throw new IllegalArgumentException("No timeslice on this dataset");

			extent = timeSliceUtil.aggregateBounds(tsList);
		} else if (diGrid.href.startsWith("epiphany")) {
			baseRsaDatasetResolution = CellSize.m100;
			extent = new Box(2125500.0, 2250100.0, 2960500.0, 2825000.0);
		} else {
			throw new IllegalArgumentException("Output grid reference should be rsa or epiphany");
		}

		List<Tile> tiles = tileManager.getTiles(extent, baseRsaDatasetResolution);

		// Inject categorisation filters.
		if (groupBy != null) {
			if (buckets == null) {
				throw new IllegalArgumentException(
						"Can't categorise: missing 'buckets' parameter.");
			}
			QueryMutator qm = new QueryMutator(qd);
			for (String group : groupBy)
				qm.addCategoriser(group, buckets);
		}

		Version ver;
		if (netcdfVersion == null)
			ver = Version.netcdf4_classic;
		else
			ver = Version.valueOf(netcdfVersion);
		ActorRef frontend = ActorCreator.getFrontend();

		JobProgress job = new JobProgress("Query (distributed)");
		job.setTaskType(TaskType.Query);
		jobProgressDao.save(job);

		for(Tile t : tiles) {
			Box bound = tileManager.getNngGrid().getBounds(t.getIndex(), baseRsaDatasetResolution);
			bound.intersect(extent);
			BoxReal bb = new BoxReal(2);
			bb.getMin().setX(bound.getXMin());
			bb.getMin().setY(bound.getYMin());
			bb.getMax().setX(bound.getXMax());
			bb.getMax().setY(bound.getYMax());
			log.info("message" + bb);
			frontend.tell(new org.vpac.worker.Job.Work(
					UUID.randomUUID().toString(), qd.toXML(), ver, bb,
					job.getId(), baseRsaDatasetResolution), ActorRef.noSender());
		}
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new QueryResponse(job.getId()));
		return "Success";
	}


	@RequestMapping(value = "/QueryOutput", method = RequestMethod.POST)
	public String queryOutput(@RequestParam(required = false) String query,
			@RequestParam(required = false) String threads,
			@RequestParam(required = false) Double minX,
			@RequestParam(required = false) Double minY,
			@RequestParam(required = false) Double maxX,
			@RequestParam(required = false) Double maxY,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String netcdfVersion,
			ModelMap model)
			throws IOException, QueryException {

		final QueryDefinition qd = QueryDefinition.fromString(query);
		if(minX != null)
			qd.output.grid.bounds = String.format("%f %f %f %f", minX, minY, maxX, maxY);

		if(startDate != null) {
			qd.output.grid.timeMin = startDate;
			qd.output.grid.timeMax = endDate;
		}

		Version version;
		if (netcdfVersion != null) {
			if (netcdfVersion.equals("nc3")) {
				version = Version.netcdf3;
			} else if (netcdfVersion.equals("nc4")) {
				version = Version.netcdf4;
			} else {
				throw new IllegalArgumentException(String.format(
						"Unrecognised NetCDF version %s", netcdfVersion));
			}
		} else {
			version = Version.netcdf4;
		}
		final Version ver = version;

		final QueryProgress qp = new QueryProgress(jobProgressDao);
		String taskId = qp.getTaskId();
		final Integer t = threads == null ? null : Integer.parseInt(threads);
		Path outputDir = FileUtils.getTargetLocation(taskId);
		final Path queryPath = outputDir.resolve("query_output.nc");
		if (!Files.exists(outputDir))
			Files.createDirectories(outputDir);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					executeQuery(qd, qp, t, queryPath, ver);
				} catch (Exception e) {
					qp.setErrorMessage(e.getMessage());
					log.error("Task exited abnormally: ", e);
				}
			}
		});

		thread.start();
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new QueryResponse(taskId));
		return "Success";
	}

	@RequestMapping(value = "/PreviewQuery", method = RequestMethod.POST)
	public void previewQuery(@RequestParam(required = false) String query,
			@RequestParam(required = false) String threads,
			@RequestParam(required = false) Double minX,
			@RequestParam(required = false) Double minY,
			@RequestParam(required = false) Double maxX,
			@RequestParam(required = false) Double maxY,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String netcdfVersion,
			ModelMap model, HttpServletResponse response)
			throws Exception {

		final QueryDefinition qd = QueryDefinition.fromString(query);
		if(minX != null)
			qd.output.grid.bounds = String.format("%f %f %f %f", minX, minY, maxX, maxY);

		if(startDate != null) {
			qd.output.grid.timeMin = startDate;
			qd.output.grid.timeMax = endDate;
		}

		Version version;
		if (netcdfVersion != null) {
			if (netcdfVersion.equals("nc3")) {
				version = Version.netcdf3;
			} else if (netcdfVersion.equals("nc4")) {
				version = Version.netcdf4;
			} else {
				throw new IllegalArgumentException(String.format(
						"Unrecognised NetCDF version %s", netcdfVersion));
			}
		} else {
			version = Version.netcdf4;
		}
		final Version ver = version;

		final QueryProgress qp = new QueryProgress(jobProgressDao);
		String taskId = qp.getTaskId();
		final Integer t = threads == null ? null : Integer.parseInt(threads);
		Path outputDir = FileUtils.getTargetLocation(taskId);
		final Path queryPath = outputDir.resolve("query_output.nc");
		if (!Files.exists(outputDir))
			Files.createDirectories(outputDir);

		List<String> actionLog = new ArrayList<String>();
		try {
			executeQuery(qd, qp, t, queryPath, ver);

			if (!Files.exists(queryPath)) {
				throw new ResourceNotFoundException("Query output file not found: " + queryPath);
			}

			Path previewPath = queryPath.getParent().resolve("preview.png");

			ImageTranslator converter = new ImageTranslator();
			// mandatory
			converter.setFormat(GdalFormat.PNG);
			converter.setLayerIndex(1);
			converter.setSrcFile(queryPath);
			converter.setDstFile(previewPath);
			converter.initialise();
			converter.execute(actionLog);

			try {
				// get your file as InputStream
				InputStream is = new FileInputStream(previewPath.toString());
				String result = DatatypeConverter.printBase64Binary(IOUtils
						.toByteArray(is));
				response.setContentType("application/json");
				response.setContentLength(result.getBytes().length);
				response.getOutputStream().write(result.getBytes());
				response.flushBuffer();
			} catch (IOException ex) {
				throw new RuntimeException(
						"IOError writing file to output stream: " + ex);
			}
		} catch (Exception e) {
			qp.setErrorMessage(e.getMessage());
			log.error("Task exited abnormally: ", e);
			log.error("Action log for preview query translation:");
			for (String line : actionLog)
				log.error("\t{}", line);
			throw e;
		}
	}


	@RequestMapping(value = "/MultiPreviewQuery", method = RequestMethod.POST)
	public void multiPreviewQuery(@RequestParam(required = false) String query,
			@RequestParam(required = false) String threads,
			@RequestParam(required = false) Double minX,
			@RequestParam(required = false) Double minY,
			@RequestParam(required = false) Double maxX,
			@RequestParam(required = false) Double maxY,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String netcdfVersion,
			ModelMap model, HttpServletResponse response)
			throws Exception {

		final QueryDefinition qd = QueryDefinition.fromString(query);
		if(minX != null)
			qd.output.grid.bounds = String.format("%f %f %f %f", minX, minY, maxX, maxY);

		if(startDate != null) {
			qd.output.grid.timeMin = startDate;
			qd.output.grid.timeMax = endDate;
		}

		Version version;
		if (netcdfVersion != null) {
			if (netcdfVersion.equals("nc3")) {
				version = Version.netcdf3;
			} else if (netcdfVersion.equals("nc4")) {
				version = Version.netcdf4;
			} else {
				throw new IllegalArgumentException(String.format(
						"Unrecognised NetCDF version %s", netcdfVersion));
			}
		} else {
			version = Version.netcdf4;
		}
		final Version ver = version;

		final QueryProgress qp = new QueryProgress(jobProgressDao);
		String taskId = qp.getTaskId();
		final Integer t = threads == null ? null : Integer.parseInt(threads);
		Path outputDir = FileUtils.getTargetLocation(taskId);
		final Path queryPath = outputDir.resolve("query_output.nc");
		if (!Files.exists(outputDir))
			Files.createDirectories(outputDir);

		try {
			executeQuery(qd, qp, t, queryPath, ver);

			if (!Files.exists(queryPath)) {
				throw new ResourceNotFoundException("Query output file not found: " + queryPath);
			}

			Path ncPreviewPath = queryPath.getParent().resolve("multipreview.nc");

			// generate multiple 2D preview out of query output
			boolean hasNoData = QueryPreviewHelper.multiPreview(queryPath, ncPreviewPath);

			Path pngPreviewPath = queryPath.getParent().resolve("multipreview.png");
			Path tifPreviewPath = queryPath.getParent().resolve("multipreview.tif");

			CommandUtil commandUtil = new CommandUtil();

			// Warp to tiff first - this sets an alpha flag.
			List<String> command = new ArrayList<String>();
			command.add("gdalwarp");
			command.add("-of");
			command.add("GTiff");
			if (hasNoData) {
				command.add("-srcnodata");
				command.add("-999");
				command.add("-dstnodata");
				command.add("-999");
				command.add("-dstalpha");
			}
			command.add(ncPreviewPath.toString());
			command.add(tifPreviewPath.toString());
			commandUtil.start(command);

			// Now translate to png.
			command = new ArrayList<String>();
			command.add("gdal_translate");
			command.add("-of");
			command.add("PNG");
			command.add("-scale");
			command.add("-ot");
			command.add("Byte");
			command.add(tifPreviewPath.toString());
			command.add(pngPreviewPath.toString());
			commandUtil.start(command);

			try {
				// get your file as InputStream
				InputStream is = new FileInputStream(pngPreviewPath.toString());
				String result = DatatypeConverter.printBase64Binary(IOUtils
						.toByteArray(is));
				response.setContentType("application/json");
				response.setContentLength(result.getBytes().length);
				response.getOutputStream().write(result.getBytes());
				response.flushBuffer();
			} catch (IOException ex) {
				throw new RuntimeException(
						"IOError writing file to output stream");
			}
		} catch (Exception e) {
			qp.setErrorMessage(e.getMessage());
			log.error("Task exited abnormally: ", e);
			throw e;
		}
	}

	private void executeQuery(QueryDefinition qd, QueryProgress qp,
			Integer threads, Path outputPath, Version netcdfVersion)
			throws IOException, QueryException {
		NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(
				netcdfVersion, outputPath.toString());

		try {
			Query q = new Query(outputDataset);
			if (threads != null)
				q.setNumThreads(threads);
				q.setMemento(qd, "preview:");
			try {
				q.setProgress(qp);
				q.run();
			} finally {
				q.close();
			}
		} finally {
			try {
				outputDataset.close();
			} catch (Exception e) {
				log.warn("Failed to close output file", e);
			}
		}
	}

	public class Pair {
		private Date date;
		private Double value;

		public Pair(Date d, Double v) {
			this.date = d;
			this.value = v;
		}

		@Override
		public String toString() {
			return String.format("[%s, %f]", date.getTime(), value);
		}
	}
}
