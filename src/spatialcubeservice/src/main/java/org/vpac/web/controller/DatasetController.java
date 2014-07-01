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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

	@RequestMapping(value = "/{datasetId}/**/categorise", method = RequestMethod.GET)
	public String createCategories(@PathVariable String datasetId,
//			@RequestParam(required = false) String[] regions,
			HttpServletRequest request, ModelMap model)
			throws ResourceNotFoundException, IOException, QueryConfigurationException {
		log.info("datasetId:" + datasetId);
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
				save(datasetId, timeSliceId, bandId, output);
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

	private void save(String datasetId, String timeSliceId, String bandId, Map<String, Foldable<?>> result) {
		for (String key : result.keySet()) {
			if (VectorCats.class.isAssignableFrom(result.get(key).getClass())) {
				String name = key;
				VectorCats value = (VectorCats) result.get(key);
				value = value.optimise();
				statisticsDao.saveCats(new DatasetCats(datasetId, timeSliceId,
						bandId, name, value.getComponents()[0]));
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

	@RequestMapping(value = "/{datasetId}/**/hist", method = RequestMethod.GET)
	public String getHistogram(@PathVariable String datasetId,
			@RequestParam(value="cat", required = false) List<Integer> categories,
			@RequestParam(required = false) String filter,
			HttpServletRequest request, ModelMap model)
			throws ResourceNotFoundException {
		log.info("datasetId: {}", datasetId);
		log.info("categories: {}", categories);
		log.info("filter: {}", filter);
		String requestURL = request.getRequestURI().toString();
		String timeSliceId = findPathVariable(requestURL, "TimeSlice");
		String bandId = findPathVariable(requestURL, "Band");

		List<DatasetCats> cats = statisticsDao.searchCats(datasetId, timeSliceId, bandId, filter);
		Dataset ds =  datasetDao.retrieve(datasetId);

		if (cats.size() > 0) {
			DatasetHistResponse result = new DatasetHistResponse(cats.get(0), ds);
			result.processSummary(categories);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, result);
		} else {
			throw new ResourceNotFoundException("No data found for this dataset, band, time slice and categorisation.");
		}

		return "List";
	}

	@RequestMapping(value = "/{datasetId}/**/cats/{type}", method = RequestMethod.GET)
	public String getCategory(@PathVariable String datasetId,
			@PathVariable String type,
			@RequestParam(required = false) List<Double> lower,
			@RequestParam(required = false) List<Double> upper,
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
		
		if (cats.size() > 0) {
			DatasetCatsResponse result = new DatasetCatsResponse(cats.get(0), ds);
			result.processSummary(lower, upper);
			model.addAttribute(ControllerHelper.RESPONSE_ROOT, result);
		} else {
			throw new ResourceNotFoundException("No data found for this dataset, band, time slice and categorisation.");
		}

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

}
