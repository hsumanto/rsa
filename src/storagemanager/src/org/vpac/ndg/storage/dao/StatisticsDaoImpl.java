package org.vpac.ndg.storage.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.util.CustomHibernateDaoSupport;

public class StatisticsDaoImpl extends CustomHibernateDaoSupport implements StatisticsDao {

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCats(TaskCats c) {
		getHibernateTemplate().save(c);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCats(DatasetCats dc) {
		getHibernateTemplate().save(dc);
	}

	@Override
	@Transactional
	public List<TaskCats> searchCats(String taskId, String catType) {
		Session session = getSession();
		Criteria c = session.createCriteria(TaskCats.class, "tc");
		c.add(Restrictions.eq("tc.taskId", taskId));
		if (catType != null)
			c.add(Restrictions.eq("tc.name", catType));
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
		List<DatasetCats> cats = c.list();
		// Ensure the objects have been fully fetched before leaving the
		// transaction.
		if (cats.size() > 0)
			cats.get(0);
		return cats;
	}

}