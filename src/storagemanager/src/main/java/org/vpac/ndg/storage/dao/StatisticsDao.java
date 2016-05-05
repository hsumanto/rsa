package org.vpac.ndg.storage.dao;

import java.util.List;

import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;

public interface StatisticsDao {
	void saveCats(TaskCats tc);
	void saveOrReplaceCats(TaskCats tc);
	List<TaskCats> searchCats(String taskId, String cattype);

	void saveCats(DatasetCats dc);
	void saveOrReplaceCats(DatasetCats dc);
	List<DatasetCats> searchCats(String datasetId, String timeSliceId, String bandId, String catType);
}
