package org.vpac.ndg.storage.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskHist;
import org.vpac.ndg.storage.util.CustomHibernateDaoSupport;

public class StatisticsDaoImpl extends CustomHibernateDaoSupport implements StatisticsDao {

	public void saveHist(TaskHist h){
		getHibernateTemplate().save(h);
	}

	public void saveCats(TaskCats c) {
		getHibernateTemplate().save(c);
	}

	@Override
	public List<TaskCats> searchCats(String taskId, String catType) {
		Session session = getSession();
		Criteria c = session.createCriteria(TaskCats.class, "tc");
		c.add(Restrictions.eq("tc.taskId", taskId));
		if(catType != null)
			c.add(Restrictions.eq("tc.name", catType));
		List<TaskCats> cats = c.list();
		return cats;
	}
	
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public void update(Dataset ds){
//		getHibernateTemplate().update(ds);
//	}
//	
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public void delete(Dataset ds){
//		getHibernateTemplate().delete(ds);
//	}
//	
//	@Transactional
//	public Dataset retrieve(String id){
//		return getHibernateTemplate().get(Dataset.class, id);
//	}
//
//	@Transactional
//	public List<Dataset> getAll() {
//		@SuppressWarnings("unchecked")
//		List<Dataset> list = getHibernateTemplate().find("FROM Dataset");
//		return list;
//	}
//
//	@Transactional
//	public List<TimeSlice> getTimeSlices(String datasetId) {
//		@SuppressWarnings("unchecked")
//		List<TimeSlice> list = getHibernateTemplate().find(
//				"SELECT s FROM Dataset d join d.slices s " +
//				"WHERE d.id=? ORDER BY s.created DESC",
//				datasetId);
//		return list;
//	}
//
//	@Transactional
//	public List<Band> getBands(String datasetId) {
//		@SuppressWarnings("unchecked")
//		List<Band> list = getHibernateTemplate().find(
//				"SELECT b FROM Dataset d JOIN d.bands b " +
//				"WHERE d.id=? ORDER BY b.name",
//				datasetId);
//		return list;
//	}
//
//	@Transactional
//	public TimeSlice findTimeSlice(String datasetId, Date creationDate) {
//		@SuppressWarnings("unchecked")
//		List<TimeSlice> list = getHibernateTemplate().find(
//				"SELECT s FROM Dataset d JOIN d.slices s " +
//				"WHERE d.id=? AND s.created=? ORDER BY s.created DESC",
//				datasetId, creationDate);
//		if(list.size() == 0)
//			return null;
//		return (TimeSlice) list.get(0);
//	}
//
//	@Transactional
//	public List<TimeSlice> findTimeSlices(String datasetId, Date startDate, Date endDate) {
//		Session session = getSession();
//		String queryString = "SELECT s FROM Dataset d JOIN d.slices s WHERE d.id=:id";
//		if (startDate != null && startDate.equals(endDate)) {
//			queryString += " AND s.created = :startDate";
//		} else {
//			if(startDate != null)
//				queryString += " AND s.created >= :startDate";
//			if(endDate != null)
//				queryString += " AND s.created < :endDate";
//		}
//
//		queryString += " ORDER BY s.created DESC";
//		Query query = session.createQuery(queryString);
//		query.setParameter("id", datasetId);
//
//		if (startDate != null && startDate.equals(endDate)) {
//			query.setParameter("startDate", startDate);
//		} else {
//			if(startDate != null)
//				query.setParameter("startDate", startDate);
//			if(endDate != null)
//				query.setParameter("endDate", endDate);
//		}
//
//		@SuppressWarnings("unchecked")
//		List<TimeSlice> list = query.list(); 
//		return list;
//	}
//
//	@Transactional
//	public Dataset findDatasetByName(String name, CellSize resolution) {
//		@SuppressWarnings("unchecked")
//		List<Dataset> list = getHibernateTemplate().find(
//				"FROM Dataset WHERE name=? AND resolution=?", name, resolution);
//		if(list.size() == 0)
//			return null;
//		return (Dataset)list.get(0);
//	}
//
//	@Transactional
//	public List<Dataset> search(String name, int page, int pageSize) {
//		Session session = getSession();
//		String queryString = "FROM Dataset";
//		if(name != null)
//			queryString += " WHERE lower(name) like :name";
//
//		queryString += " ORDER BY created DESC";
//		Query query = session.createQuery(queryString);
//		if(name != null)
//			query.setString("name", "%" + name.toLowerCase() + "%");
//		
//		query.setFirstResult(page * pageSize);
//		query.setMaxResults(pageSize);
//
//		@SuppressWarnings("unchecked")
//		List<Dataset> list = query.list();
//		return list;
//	}
//
//	@Transactional
//	public List<Dataset> search(String name, CellSize resolution) {
//		Session session = getSession();
//		String queryString = "FROM Dataset";
//		if(name != null && resolution != null) {
//			queryString += " WHERE name like :name and resolution = :resolution";
//		}
//		else if(name != null && resolution == null) {
//			queryString += " WHERE lower(name) like :name";
//		}
//		else if(name == null && resolution != null) {
//			queryString += " WHERE resolution = :resolution";
//		}
//
//		queryString += " ORDER BY created DESC";
//
//		Query query = session.createQuery(queryString);
//		if(name != null) {
//			query.setString("name", "%" + name.toLowerCase() + "%");
//		}
//
//		if(resolution != null) {
//			query.setParameter("resolution", resolution);
//		}
//
//		@SuppressWarnings("unchecked")
//		List<Dataset> list = query.list();
//		return list;
//	}
//
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public void addBand(String datasetId, Band band) {
//		Session session = getSession();
//		Dataset ds = (Dataset) session.get(Dataset.class, datasetId);
//		if(ds == null) {
//			// Capture if dataset not exist
//			String msg = String.format("Dataset with ID = %s not found.", datasetId);
//			throw new IllegalArgumentException(msg);
//		}
//
//		if(ds.getBands() == null)
//			ds.setBands(new ArrayList<Band>());
//		ds.getBands().add(band);
//		session.saveOrUpdate(band);
//		session.saveOrUpdate(ds);
//	}
//
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public TimeSlice addTimeSlice(String datasetId, TimeSlice ts) {
//		Session session = getSession();
//		Dataset ds = (Dataset) session.get(Dataset.class, datasetId);
//		if(ds == null) {
//			// Capture if dataset not exist
//			String msg = String.format("Dataset with ID = %s not found.", datasetId);
//			throw new IllegalArgumentException(msg);
//		}
//
//		if(ds.getSlices() == null)
//			ds.setSlices(new ArrayList<TimeSlice>());
//		ds.getSlices().add(ts);
//		session.saveOrUpdate(ds);
//		session.saveOrUpdate(ts);
//		return ts;
//	}
//
//	@Override
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public void addAllBands(String datasetId, List<Band> bands) {
//		Dataset ds = retrieve(datasetId);
//		if(ds == null) {
//			// Capture if dataset not exist
//			String msg = String.format("Dataset with ID = %s not found.", datasetId);
//			throw new IllegalArgumentException(msg);
//		}
//
//		for(Band band : bands) {
//			// Only add new band
//			if(!ds.hasBand(band)) {
//				addBand(datasetId, band);
//			}
//		}
//	}
//
//	@Override
//	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
//	public void addAllSlices(String datasetId, List<TimeSlice> slices) {
//		Dataset ds = retrieve(datasetId);
//		if(ds == null) {
//			// Capture if dataset not exist
//			String msg = String.format("Dataset with ID = %s not found.", datasetId);
//			throw new IllegalArgumentException(msg);
//		}
//
//		for(TimeSlice ts : slices) {
//			// Only add new timeslice
//			if(findTimeSlice(datasetId, ts.getCreated()) == null) {
//				addTimeSlice(datasetId, ts);
//			}
//		}
//	}
//
//	@Transactional
//	@Override
//	public List<Band> findBands(String datasetId, List<String> bandIds) {
//		Session session = getSession();
//		String queryString = "SELECT b FROM Dataset d JOIN d.bands b WHERE d.id=:id AND b.id IN (:bandIds) ORDER BY b.name";
//		Query query = session.createQuery(queryString);
//		query.setParameter("id", datasetId);
//		query.setParameterList("bandIds", bandIds);
//
//		@SuppressWarnings("unchecked")
//		List<Band> list = query.list();
//		return list;
//	}
//
//	@Transactional
//	@Override
//	public List<Band> findBandsByName(String datasetId, List<String> bandNames) {
//		Session session = getSession();
//		String queryString = "SELECT b FROM Dataset d JOIN d.bands b WHERE d.id=:id AND b.name IN (:bandNames) ORDER BY b.name";
//		Query query = session.createQuery(queryString);
//		query.setParameter("id", datasetId);
//		query.setParameterList("bandNames", bandNames);
//
//		@SuppressWarnings("unchecked")
//		List<Band> list = query.list();
//		return list;
//	}
}