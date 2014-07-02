package org.vpac.ndg.storage.dao;

import java.util.List;

import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;

public interface StatisticsDao {
	public void saveCats(TaskCats tc);
	public void saveCats(DatasetCats dc);
	public List<TaskCats> searchCats(String taskId, String cattype);
	public List<DatasetCats> searchCats(String datasetId, String timeSliceId, String bandId, String catType);
}
