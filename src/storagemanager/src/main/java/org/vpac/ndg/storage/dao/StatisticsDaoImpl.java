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
 *
 * Copyright 2016 VPAC Innovations
 */

package org.vpac.ndg.storage.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskLedger;
import org.vpac.ndg.storage.util.CustomHibernateDaoSupport;

public class StatisticsDaoImpl extends CustomHibernateDaoSupport implements StatisticsDao {

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCats(TaskCats c) {
		getSession().save(c);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCats(DatasetCats dc) {
		getSession().save(dc);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveOrReplaceCats(TaskCats tc) {
		for (TaskCats oldTc : searchCats(tc.getTaskId(), tc.getName())) {
			getSession().delete(oldTc);
		}
		saveCats(tc);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveOrReplaceCats(DatasetCats dc) {
		for (DatasetCats oldDc : searchCats(dc.getDatasetId(), dc.getTimeSliceId(), dc.getBandId(), dc.getName())) {
			getSession().delete(oldDc);
		}
		saveCats(dc);
	}

	@Override
	@Transactional
	public List<TaskCats> searchCats(String taskId, String catType) {
		Session session = getSession();
		Criteria c = session.createCriteria(TaskCats.class, "tc");
		c.add(Restrictions.eq("tc.taskId", taskId));
		if (catType != null)
			c.add(Restrictions.eq("tc.name", catType));
		@SuppressWarnings("unchecked")
		List<TaskCats> cats = c.list();
		// Ensure the objects have been fully fetched before leaving the
		// transaction.
		if (cats.size() > 0)
			cats.get(0);
		return cats;
	}

	@Override
	@Transactional
	public List<DatasetCats> searchCats(String datasetId, String timeSliceId,
			String bandId, String catType) {
		Session session = getSession();
		Criteria c = session.createCriteria(DatasetCats.class, "dc");
		c.add(Restrictions.eq("dc.datasetId", datasetId));
		if (timeSliceId != null)
			c.add(Restrictions.eq("dc.timeSliceId", timeSliceId));
		if (bandId != null)
			c.add(Restrictions.eq("dc.bandId", bandId));
		if (catType != null)
			c.add(Restrictions.eq("dc.name", catType));
		@SuppressWarnings("unchecked")
		List<DatasetCats> cats = c.list();
		// Ensure the objects have been fully fetched before leaving the
		// transaction.
		if (cats.size() > 0)
			cats.get(0);
		return cats;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void save(Ledger l) {
		getSession().save(l);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveLedger(TaskLedger tl) {
		getSession().save(tl);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Ledger getLedger(String id) {
		return getSession().get(Ledger.class, id);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveOrReplaceLedger(TaskLedger tl) {
		TaskLedger oldTl = getTaskLedger(tl.getId());
		if (oldTl != null)
			getSession().delete(oldTl);
		saveLedger(tl);
	}

	@Override
	@Transactional
	public TaskLedger getTaskLedger(String jobId) {
		return getSession().get(TaskLedger.class, jobId);
	}
}
