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

package org.vpac.ndg.storage;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TaskLedger;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/config/TestBeanLocations.xml" })
@Transactional
public class StatisticsDaoTest {

	final private Logger log = LoggerFactory.getLogger(StatisticsDaoTest.class);

	@Autowired
	StatisticsDao statisticsDao;

	@Autowired
	JobProgressDao jobProgressDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void testStoreLedger() {
		Session session = sessionFactory.getCurrentSession();

		Ledger ledger = new Ledger();
		ledger.setBucketingStrategies(
			Arrays.asList("categorical", "categorical"));
		ledger.add(Arrays.asList(0.0, 1.0));
		ledger.add(Arrays.asList(0.0, 1.0));
		ledger.add(Arrays.asList(1.0, 1.0));
		statisticsDao.save(ledger);
		session.flush();

		JobProgress job = new JobProgress("testStoreLedger");
		job.setTaskType(TaskType.Query);
		jobProgressDao.save(job);
		session.flush();

		TaskLedger tl = new TaskLedger();
		tl.setJob(job);
		tl.setLedger(ledger);
		statisticsDao.saveLedger(tl);
		session.flush();

		List<TaskLedger> tls = statisticsDao.searchLedger(job.getId());
		assertEquals(tls.size(), 1);
	}
}
