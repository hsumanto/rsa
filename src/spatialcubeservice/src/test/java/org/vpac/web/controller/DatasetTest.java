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
 * Copyright 2016 VPAC Innovations
 */

package org.vpac.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.vpac.web.model.response.DatasetCollectionResponse;
import org.vpac.web.model.response.DatasetResponse;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class DatasetTest extends WebServiceTestBase {
	final String BASE_URL = "http://localhost";

	RestTemplate restTemplate;

	@Before
	public void setUp() throws Exception {
		super.setUp();
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
			.andExpect(xpath("/Dataset/@precision").number(1.0));
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

		MvcResult response;
		DatasetCollectionResponse result;

		response = mockMvc
			.perform(get("/Dataset.xml"))
			.andExpect(status().isOk())
			.andReturn();
		result = (DatasetCollectionResponse) response.getModelAndView()
			.getModel().get("Response");
		assertThat(result.getItems().size(), greaterThan(0));

		response = mockMvc
			.perform(get("/Dataset.xml")
				.param("name", "DatasetTest"))
			.andExpect(status().isOk())
			.andReturn();
		result = (DatasetCollectionResponse) response.getModelAndView()
			.getModel().get("Response");
		assertThat(result.getItems().size(), is(3));
	}

	@Test(expected=Exception.class)
	public void testPageParameterValidatingPage() throws Exception {
		createDataset("DatasetTest1", "500m", "foo", "1");
		createDataset("DatasetTest2", "500m", "foo", "1");
		createDataset("DatasetTest3", "500m", "foo", "1");
		DatasetCollectionResponse response;
		String testURL;
		testURL = BASE_URL + "/Dataset.xml?page=-1&pageSize=2";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(0));
		// Should be resulted in 500 (Internal Server Error) because page is rejected.
	}

	@Test(expected=Exception.class)
	public void testPageParameterValidatingPageSize() throws Exception {
		createDataset("DatasetTest1", "500m", "foo", "1");
		createDataset("DatasetTest2", "500m", "foo", "1");
		createDataset("DatasetTest3", "500m", "foo", "1");
		DatasetCollectionResponse response;
		String testURL;
		testURL = BASE_URL + "/Dataset.xml?page=1&pageSize=-2";
		response = restTemplate.getForObject(testURL, DatasetCollectionResponse.class);
		assertThat(response.getItems().size(), is(2));
		// Should be resulted in 500 (Internal Server Error) because pageSize is rejected.
	}

	@Test
	public void testGetDatasetById() throws Exception {
		String id = createDataset("DatasetTest1", "500m", "foo", "1").getId();
		String testURL = BASE_URL + "/Dataset/{id}.xml";
		DatasetResponse response = restTemplate.getForObject(testURL, DatasetResponse.class, id);
		assertEquals(response.getId(), id);
	}

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
	public void testCreateDatasetAbnormalResolution() throws Exception {
		mockMvc.perform(post("/Dataset.xml")
				.param("name", "DatasetTest1")
				.param("resolution", "51m")
				.param("dataAbstract", "bar")
				.param("precision", "1"))
			.andExpect(status().isBadRequest());
		// Should be resulted in 500 (Internal Server Error) because resolution is rejected.
	}
}
