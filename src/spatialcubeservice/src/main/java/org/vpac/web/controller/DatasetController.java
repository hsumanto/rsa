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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.vpac.ndg.FileUtils;
import org.vpac.ndg.Utils;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.Query;
import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;
import org.vpac.ndg.query.stats.VectorCats;
import org.vpac.ndg.storage.dao.BandDao;
import org.vpac.ndg.storage.dao.DatasetDao;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.model.Band;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.web.exception.ResourceNotFoundException;
import org.vpac.web.model.request.DatasetRequest;
import org.vpac.web.model.request.PagingRequest;
import org.vpac.web.model.response.DatasetCatsResponse;
import org.vpac.web.model.response.DatasetCollectionResponse;
import org.vpac.web.model.response.DatasetHistResponse;
import org.vpac.web.model.response.DatasetResponse;
import org.vpac.web.model.response.QueryResponse;
import org.vpac.web.util.ControllerHelper;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;

@Controller
@RequestMapping("/Dataset")
public class DatasetController {

	final private Logger log = LoggerFactory.getLogger(DatasetController.class);

	@Autowired
	ControllerHelper helper;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	BandDao bandDao;
	
	@Autowired
	JobProgressDao jobProgressDao;

	@Autowired
	StatisticsDao statisticsDao;
	
	@Autowired
	DatasetUtil datasetUtil;

	@Autowired
	DatasetProvider rsaDatasetProvider;

	@Autowired
	DatasetProvider previewDatasetProvider;
	
	
	@InitBinder
	public void binder(WebDataBinder binder) {
		helper.BindDateTimeFormatter(binder);
		helper.BindCellSizeFormatter(binder);
		ProviderRegistry.getInstance().clearProivders();
		ProviderRegistry.getInstance().addProvider(rsaDatasetProvider);
		ProviderRegistry.getInstance().addProvider(previewDatasetProvider);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getAllDataset(@RequestParam(required = false) String name,
			@Valid PagingRequest page, ModelMap model) {
		List<Dataset> list = datasetDao.search(name, page.getPage(),
				page.getPageSize());
		model.addAttribute(ControllerHelper.RESPONSE_ROOT,
				new DatasetCollectionResponse(list));
		return "List";
	}

	@RequestMapping(value = "/Search", method = RequestMethod.GET)
	public String searchDataset(@RequestParam(required = false) String name,
			@RequestParam(required = false) String resolution, ModelMap model) {
		List<Dataset> list = datasetDao.search(name,
				CellSize.fromHumanString(resolution));
		model.addAttribute(ControllerHelper.RESPONSE_ROOT,
				new DatasetCollectionResponse(list));
		return "List";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getDatasetById(@PathVariable String id, ModelMap model)
			throws ResourceNotFoundException {
		Dataset ds = datasetDao.retrieve(id);
		if (ds == null) {
			// Capture if dataset not exist
			throw new ResourceNotFoundException(String.format(
					"Dataset with ID = \"%s\" not found.", id));
		}
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new DatasetResponse(
				ds));
		return "List";
	}

	@RequestMapping(value = "/{datasetId}/**/hist", method = RequestMethod.GET)
	public String getHistogram(@PathVariable String datasetId,
			@RequestParam(required = false) List<Integer> categories,
			@RequestParam(required = false) String type,
			HttpServletRequest request, ModelMap model)
			throws ResourceNotFoundException {
		log.info("datasetId:" + datasetId);
//		log.info("categories:" + categories.toString());
		log.info("type:" + type);
		String requestURL = request.getRequestURI().toString();
		String timeSliceId = findPathVariable(requestURL, "TimeSlice");
		String bandId = findPathVariable(requestURL, "Band");
		
		List<DatasetCats> cats = statisticsDao.searchCats(datasetId, timeSliceId, bandId, type);
		
		if(cats != null) {
			DatasetHistResponse result = new DatasetHistResponse(cats.get(0));
			result.processSummary(categories);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, result);
		} else
			throw new ResourceNotFoundException("No data not found.");

		return "List";
	}

	@RequestMapping(value = "/{datasetId}/**/categorise", method = RequestMethod.GET)
	public String createCategories(@PathVariable String datasetId,
//			@RequestParam(required = false) String[] regions,
			HttpServletRequest request, ModelMap model)
			throws ResourceNotFoundException, IOException, QueryConfigurationException {
		log.info("datasetId:" + datasetId);
//		log.info("categories:" + regions.toString());
		String requestURL = request.getRequestURI().toString();
		String timeSliceId = findPathVariable(requestURL, "TimeSlice");
		String bandId = findPathVariable(requestURL, "Band");
		
		Dataset ds = datasetDao.retrieve(datasetId);
		Band band = bandDao.retrieve(bandId);

		final QueryDefinition qd = QueryDefinition.fromXML(Thread
				.currentThread().getContextClassLoader()
				.getResourceAsStream("categorise.xml"));
		qd.inputs.get(0).href= "rsa:" + ds.getName() + "/" + ds.getResolution().toHumanString();
		qd.filters.get(0).samplers.get(0).ref = "#ds/" + band.getName();
		// if(minX != null)
		// qd.output.grid.bounds = String.format("%f %f %f %f", minX, minY,
		// maxX, maxY);
		//
		// if(startDate != null) {
		// qd.output.grid.timeMin = startDate;
		// qd.output.grid.timeMax = endDate;
		// }

		final Version ver = Version.netcdf4_classic;

		final QueryProgress qp = new QueryProgress(jobProgressDao);
		String taskId = qp.getTaskId();
		Path outputDir = FileUtils.getTargetLocation(taskId);
		if (!Files.exists(outputDir))
			Files.createDirectories(outputDir);
		Path outputPath = outputDir.resolve(taskId + "_out.nc");

		NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(
				ver, outputPath.toString());

		Map<String, Foldable<?>> output = null;
		try {
			Query q = new Query(outputDataset);
			q.setNumThreads(1);
			q.setMemento(qd, new File(".").getAbsolutePath());
			try {
				q.setProgress(qp);
				q.run();
				output = q.getAccumulatedOutput();
				foldResults(datasetId, timeSliceId, bandId, output);
			} finally {
				q.close();
			}
		} finally {
			try {
				outputDataset.close();
			} catch (Exception e) {
				log.error("Failed to close output file", e);
			}
		}

		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new QueryResponse(
				taskId));
		return "Success";
	}
	private void foldResults(String datasetId, String timeSliceId, String bandId, Map<String, Foldable<?>> output) {
		Map<String, Foldable<? extends Serializable>> result  = filterOutNonSerializable(output);
		Map<String, Foldable> resultMap = new HashMap<>();
		for(Entry<String, ?> v : result.entrySet()) {
			String key = v.getKey();
			Foldable value = (Foldable) v.getValue();
			if(resultMap.get(key) == null) {
				resultMap.put(key, new VectorCats(1));
			}
			resultMap.put(key, resultMap.get(key).fold(value));
		}
		save(datasetId, timeSliceId, bandId, resultMap);
	}

	private Map<String, Foldable<? extends Serializable>> filterOutNonSerializable(Map<String, Foldable<?>> output) {
		Map<String, Foldable<? extends Serializable>> result = new HashMap<>();
		for(Entry<String, ?> v : output.entrySet()) {
			if(Serializable.class.isAssignableFrom(v.getValue().getClass()))
				result.put(v.getKey(), (Foldable<? extends Serializable>) v.getValue());
		}
		return result;
	}
	
	private void save(String datasetId, String timeSliceId, String bandId, Map<String, Foldable> result) {
		for(String key : result.keySet()) {
			if (VectorCats.class.isAssignableFrom(result.get(key).getClass())) {
				String name = key;
				CellSize outputResolution = CellSize.m25;
				statisticsDao.saveCats(new DatasetCats(datasetId, timeSliceId, bandId, name, ((VectorCats)result.get(key)).getComponents()[0]));
//			} else 	if (VectorHist.class.isAssignableFrom(f.getClass())) {
//				String name = "lga";
//				CellSize outputResolution = CellSize.m25;
//				statisticsDao.saveHist(new TaskHist(currentWorkInfo.work.jobProgressId, name, outputResolution,((VectorHist)f).getComponents()[0]));
			}
		}
	}

	
	private String findPathVariable(String url, String varName) {
		String returnValue = null;
		int timeSliceLocation = url.indexOf(varName);
		if (timeSliceLocation > 0) {
			String TimeSlicePart = url.substring(timeSliceLocation
					+ varName.length() + 1);
			returnValue = TimeSlicePart.split("/")[0];
		}
		return returnValue;
	}

	@RequestMapping(value = "/{datasetId}/**/cats/{type}", method = RequestMethod.GET)
	public String getCategory(@PathVariable String datasetId,
			@PathVariable String type,
			@RequestParam(required = false) Double lower,
			@RequestParam(required = false) Double upper,
			HttpServletRequest request, ModelMap model)
			throws ResourceNotFoundException {
		log.info("datasetId:" + datasetId);
		log.info("type:" + type);
		log.info("lower:" + lower);
		log.info("upper:" + upper);
		String requestURL = request.getRequestURI().toString();
		String timeSliceId = findPathVariable(requestURL, "TimeSlice");
		String bandId = findPathVariable(requestURL, "Band");

		List<DatasetCats> cats = statisticsDao.searchCats(datasetId, timeSliceId, bandId, type);
		Dataset ds =  datasetDao.retrieve(datasetId);
		
		if(cats != null) {
			DatasetCatsResponse result = new DatasetCatsResponse(cats.get(0), ds);
			result.processSummary(lower, upper);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, result);
		} else
			throw new ResourceNotFoundException("No data not found.");

		return "List";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String createOrUpdateDataset(@Valid DatasetRequest dr, ModelMap model)
			throws ResourceNotFoundException, IOException {

		log.info("Create / Update Dataset");
		log.debug("Id: {}", dr.getId());
		log.debug("Name: {}", dr.getName());
		log.debug("Resolution: {}", dr.getResolution());
		log.debug("Precision: {}", dr.getPrecision());
		log.debug("Abstract: {}", dr.getDataAbstract());

		long precision = Utils.parseTemporalPrecision(dr.getPrecision());
		if (dr.getId() == null || dr.getId().isEmpty()) {
			Dataset newDataset = new Dataset(dr.getName(),
					dr.getDataAbstract(), dr.getResolution(), precision);
			datasetDao.create(newDataset);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT,
					new DatasetResponse(newDataset));
		} else {
			Dataset ds = datasetDao.retrieve(dr.getId());
			if (ds == null)
				throw new ResourceNotFoundException(String.format(
						"Dataset with ID = \"%s\" not found.", dr.getId()));
			ds.setAbst(dr.getDataAbstract());
			ds.setResolution(dr.getResolution());
			ds.setPrecision(Long.parseLong(dr.getPrecision()));
			if (ds.getName().equals(dr.getName()))
				datasetDao.update(ds);
			else {
				ds.setName(dr.getName());
				datasetUtil.update(ds);
			}
			model.addAttribute(ControllerHelper.RESPONSE_ROOT,
					new DatasetResponse(ds));
		}

		return "Success";
	}

	@RequestMapping(value = "/Delete/{id}", method = RequestMethod.POST)
	public String deleteDataset(@PathVariable String id, ModelMap model)
			throws ResourceNotFoundException, IOException {
		Dataset ds = datasetDao.retrieve(id);
		if (ds == null) {
			// Capture if dataset not exist
			throw new ResourceNotFoundException(String.format(
					"Dataset with ID = \"%s\" not found.", id));
		}
		datasetUtil.deleteDataset(ds);
		model.addAttribute(ControllerHelper.RESPONSE_ROOT, new DatasetResponse(
				ds));
		return "Success";
	}

	@RequestMapping(value = "/Form", method = RequestMethod.GET)
	public String createTestForm() {
		return "DatasetForm";
	}

	private void executeQuery(QueryDefinition qd, QueryProgress qp,
			Integer threads, Path outputPath, Version netcdfVersion)
			throws IOException, QueryConfigurationException {
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
}
