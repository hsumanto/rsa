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
 * Copyright 2016 VPAC Innovations
 */

package org.vpac.web.controller;

import java.util.Arrays;
import java.util.List;
import org.hibernate.SessionFactory;
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
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.query.iteration.Pair;
import org.vpac.ndg.query.math.ElementInt;
import org.vpac.ndg.query.stats.BucketingStrategyLog;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskLedger;
import org.vpac.web.model.response.DatasetCollectionResponse;
import org.vpac.web.model.response.DatasetResponse;
import org.vpac.web.util.ControllerHelper;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class StatisticsTest extends WebServiceTestBase {

	@Autowired
	StatisticsDao statisticsDao;

	@Autowired
	JobProgressDao jobProgressDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void testCats() throws Exception {

		ElementInt category = new ElementInt();
		ElementInt value = new ElementInt();
		List<Pair<Integer, Integer>> permutations = Arrays.asList(
			new Pair<Integer, Integer>(1, 1),
			new Pair<Integer, Integer>(1, 2),
			new Pair<Integer, Integer>(1, 3),
			new Pair<Integer, Integer>(2, 3),
			new Pair<Integer, Integer>(2, 3),
			new Pair<Integer, Integer>(2, 4));

		Cats cats = new Cats();
		cats.setBucketingStrategy(new BucketingStrategyLog());
		for (Pair<Integer, Integer> p : permutations) {
			category.set(p.a);
			value.set(p.a * p.b);
			cats.update(category, value);
		}

		JobProgress job = new JobProgress("testStoreLedger");
		job.setTaskType(TaskType.Query);
		jobProgressDao.save(job);
		sessionFactory.getCurrentSession().flush();

		TaskCats tc = new TaskCats();
		tc.setTaskId(job.getId());
		tc.setCats(cats);
		tc.setName("foo");
		tc.setOutputResolution(CellSize.m100);
		statisticsDao.saveCats(tc);

		// Ensure objects have IDs
		sessionFactory.getCurrentSession().flush();
		System.out.println(job.getId());

		mockMvc.perform(get(
					"/Data/Task/{tid}/table/foo.xml", job.getId())
				.param("name", "foo"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(xpath("/Table/@tableType").string("categories"))
			.andExpect(xpath("/Table/@categorisation").string("foo"))
			.andExpect(xpath("/Table/columns").nodeCount(3))
			.andExpect(xpath("/Table/rows").nodeCount(2));
	}

	@Test
	public void testLedger() throws Exception {
		Ledger ledger = new Ledger();
		ledger.setBucketingStrategies(
			Arrays.asList("categorical", "categorical"));
		ledger.add(Arrays.asList(0.0, 1.0));
		ledger.add(Arrays.asList(1.0, 2.0));
		ledger.add(Arrays.asList(0.0, 1.0));
		ledger.add(Arrays.asList(1.0, 2.0));
		ledger.add(Arrays.asList(1.0, 1.0));
		ledger.add(Arrays.asList(1.0, 2.0));
		statisticsDao.save(ledger);

		JobProgress job = new JobProgress("testStoreLedger");
		job.setTaskType(TaskType.Query);
		jobProgressDao.save(job);

		TaskLedger tl = new TaskLedger();
		tl.setJob(job);
		tl.setLedger(ledger);
		tl.setKey("foo");
		tl.setOutputResolution(CellSize.m100);
		statisticsDao.saveLedger(tl);

		// Ensure objects have IDs
		sessionFactory.getCurrentSession().flush();
		System.out.println(job.getId());

		mockMvc.perform(get(
					"/Data/Task/{tid}/table.xml", job.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(xpath("/Table/@tableType").string("ledger"))
			.andExpect(xpath("/Table/@categorisation").string("foo"))
			.andExpect(xpath("/Table/columns").nodeCount(4));

		mockMvc.perform(get(
					"/Data/Task/{tid}/table.json", job.getId()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tableType", is("ledger")))
			.andExpect(jsonPath("$.categorisation", is("foo")))
			.andExpect(jsonPath("$.columns", hasSize(4)));
	}
}
