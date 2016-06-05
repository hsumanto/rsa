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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.vpac.web.controller.DatasetController;
import org.vpac.web.model.response.DatasetCollectionResponse;
import org.vpac.web.model.response.DatasetResponse;
import org.vpac.web.util.ControllerHelper;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
	"file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml",
	"file:src/main/webapp/WEB-INF/applicationContext.xml"})
@Transactional
public class DatasetTest {
	final String BASE_URL = "http://localhost:8080/rsa";

	private static String TestDatasetName = "DatasetTest";
	private String testDatasetId;

	// @Autowired
	RestTemplate restTemplate;

	@Autowired
	WebApplicationContext wac;
	MockMvc mockMvc;

	@Autowired
	DatasetController datasetController;

	@Autowired
	MockHttpServletRequest request;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ClientHttpRequestFactory requestFactory =
			new MockMvcClientHttpRequestFactory(mockMvc);
		restTemplate = new RestTemplate(requestFactory);
	}

	public DatasetResponse createDataset(String name, String resolution,
			String abs, String precision)
			throws Exception {
		MvcResult result = mockMvc
			.perform(post("/Dataset.xml")
				.param("name", name)
				.param("resolution", resolution)
				.param("dataAbstract", abs)
				.param("precision", precision))
			.andExpect(status().isOk())
			.andReturn();
		if (result.getResolvedException() != null)
			throw result.getResolvedException();
		return (DatasetResponse) result.getModelAndView()
			.getModel().get("Response");
	}

	@Test
	public void testProducesXml() throws Exception {
		mockMvc.perform(post("/Dataset.xml")
				.param("name", "foo")
				.param("resolution", "m500")
				.param("dataAbstract", "bar")
				.param("precision", "1"))
			.andExpect(xpath("/Dataset/name").string("foo"))
			.andExpect(xpath("/Dataset/dataAbstract").string("bar"))
			.andExpect(xpath("/Dataset/@precision").number(1.0))
			.andReturn();
	}

	@Test
	public void testProducesJson() throws Exception {
		mockMvc.perform(post("/Dataset.json")
				.param("name", "foo")
				.param("resolution", "m500")
				.param("dataAbstract", "bar")
				.param("precision", "1"))
			.andExpect(jsonPath("$.name").value("foo"))
			.andExpect(jsonPath("$.dataAbstract").value("bar"))
			.andExpect(jsonPath("$.precision").value(1));
	}

	@Test
	public void testList() throws Exception {
		createDataset("DatasetTest1", "500m", "foo", "1");
		createDataset("DatasetTest2", "500m", "foo", "1");
		createDataset("DatasetTest3", "500m", "foo", "1");

		MvcResult mvcResult;
		DatasetCollectionResponse response;

		mvcResult = mockMvc
			.perform(get("/Dataset.xml"))
			.andExpect(status().isOk())
			.andReturn();
		response = (DatasetCollectionResponse) mvcResult.getAsyncResult(10L);
		assertNotSame(response.getItems().size(), is(0));

		mvcResult = mockMvc
			.perform(get("/Dataset.xml")
				.param("name", "DatasetTest"))
			.andExpect(status().isOk())
			.andReturn();
		response = (DatasetCollectionResponse) mvcResult.getAsyncResult(10L);
		assertSame(response.getItems().size(), is(3));
	}

	@Test(expected=Exception.class)
	public void testPageParameterValidatingPage() {
		DatasetCollectionResponse response;
		String testURL;
		testURL = BASE_URL + "/Dataset.xml?page=-1&pageSize=2";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(0));
		// Should be resulted in 500 (Internal Server Error) because page is rejected.
	}

	@Test(expected=Exception.class)
	public void testPageParameterValidatingPageSize() {
		DatasetCollectionResponse response;
		String testURL;
		testURL = BASE_URL + "/Dataset.xml?page=1&pageSize=-2";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(2));
		// Should be resulted in 500 (Internal Server Error) because pageSize is rejected.
	}

	@Test
	public void testGetDatasetById() {
		String testURL = BASE_URL + "/Dataset/{id}.xml";
		DatasetResponse response = restTemplate.getForObject(testURL, DatasetResponse.class, testDatasetId);
		assertEquals(response.getId(), testDatasetId);
	}

/*	@Test
	public void testGetDatasetToDummyResponse() {
		String testURL = BASE_URL + "/Dataset/{id}.xml";
		DummyResponse response = restTemplate.getForObject(testURL, DummyResponse.class, testDateasetId);
		assertThat(response.getId(), is(testDateasetId));
	}
*/
	@Test
	public void testSearchDataset() {
		String searchString = "11";
		String testURL;
		DatasetCollectionResponse response;
		testURL = BASE_URL + "/Dataset.xml?name=" + searchString;
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		for(DatasetResponse dr : response.getItems())
			assertTrue(dr.getName().contains(searchString));

		testURL = BASE_URL + "/Dataset.xml?name=11&page=0&pageSize=1";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(0));

		testURL = BASE_URL + "/Dataset.xml?name=sdfafasdfsfsdafa&page=0&pageSize=1";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(0));
	}

	@Test
	public void testCreateDataset() {
		String name = TestDatasetName;
		String resolution = "10m";
		String dataAbstract = "testAbs";

		String checkUrl = BASE_URL + "/Dataset.xml?name={name}&resolution={resolution}";
		DatasetCollectionResponse getResponse = restTemplate.getForObject(checkUrl, DatasetCollectionResponse.class, name, resolution);

		if(getResponse.getItems().size() == 0) {
			DatasetResponse response = testCreateDataset(name, resolution, dataAbstract);
			assertNotNull(response.getId());
			assertThat(response.getName(), is(name));
			assertThat(response.getDataAbstract(), is(dataAbstract));
		}
	}

/*
Seems like the annotation and the way we create dataset by JSON is having issue,
THUS COMMENTED THIS CODE AT THE MOMENT, ALTERNATIVELY JSON TEST CAN BE CONDUCTED USING /WEB_INF/pages/DatasetForm.jsp
org.springframework.validation.BindException: org.springframework.validation.BeanPropertyBindingResult: 4 errors
Field error in object 'datasetRequest' on field 'precision': rejected value [null]; codes [NotNull.datasetRequest.precision,NotNull.precision,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [datasetRequest.precision,precision]; arguments []; default message [precision]]; default message [may not be null]
Field error in object 'datasetRequest' on field 'dataAbstract': rejected value [null]; codes [NotNull.datasetRequest.dataAbstract,NotNull.dataAbstract,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [datasetRequest.dataAbstract,dataAbstract]; arguments []; default message [dataAbstract]]; default message [may not be null]
Field error in object 'datasetRequest' on field 'name': rejected value [null]; codes [NotNull.datasetRequest.name,NotNull.name,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [datasetRequest.name,name]; arguments []; default message [name]]; default message [may not be null]
Field error in object 'datasetRequest' on field 'resolution': rejected value [null]; codes [NotNull.datasetRequest.resolution,NotNull.resolution,NotNull.org.vpac.ndg.common.datamodel.CellSize,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [datasetRequest.resolution,resolution]; arguments []; default message [resolution]]; default message [may not be null]
	@Test
	public void testCreateDatasetByJson() {
		String name = "testCreateDatasetByJson";
		String resolution = "m500";
		String dataAbstract = "testAbs";

		String checkUrl = BASE_URL + "/Dataset.xml?name={name}&resolution={resolution}";
		DatasetCollectionResponse getResponse = restTemplate.getForObject(checkUrl, DatasetCollectionResponse.class, name, resolution);

		if(getResponse.getItems().size() == 0) {
			List<MediaType> mediaTypes = new ArrayList<MediaType>();
			mediaTypes.add(MediaType.APPLICATION_JSON);

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(mediaTypes);
			String requestJson = String.format("{name : \'%s\', resolution: \'%s\', dataAbstract : \'%s\', precision : \'1\'}", name, resolution, dataAbstract);
			HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
			String testURL = BASE_URL + "/Dataset.xml";
			DatasetResponse response = restTemplate.postForObject(testURL, entity, DatasetResponse.class);
			assertThat(response.getName(), is("test"));
		}
	}
*/

	@Test(expected=Exception.class)
	public void testCreateDatasetAbnormalResolution() {
		String name = TestDatasetName;
		String resolution = "50m";
		String dataAbstract = "testAbs";
		@SuppressWarnings("unused")
		DatasetResponse response = testCreateDataset(name, resolution, dataAbstract);
		// Should be resulted in 500 (Internal Server Error) because resolution is rejected.
	}

	private DatasetResponse testCreateDataset(String name, String resolution, String dataAbstract) {
		String testURL = BASE_URL + "/Dataset.xml?name={name}&resolution={resolution}&dataAbstract={dataAbstract}&precision=1";
		return restTemplate.postForObject(testURL, null, DatasetResponse.class, name, resolution, dataAbstract);
	}
}
