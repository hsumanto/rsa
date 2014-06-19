package org.vpac.ndg.storage.dao;

import java.util.List;

import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskHist;

public interface StatisticsDao {
	public void saveHist(TaskHist th);
	public void saveCats(TaskCats tc);
	public void saveCats(DatasetCats dc);
	public List<TaskCats> searchCats(String taskId, String cattype);
}
