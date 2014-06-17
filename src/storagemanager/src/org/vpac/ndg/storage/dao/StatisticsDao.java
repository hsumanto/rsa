package org.vpac.ndg.storage.dao;

import java.util.List;

import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskHist;

public interface StatisticsDao {
	public void saveHist(TaskHist th);
	public void saveCats(TaskCats tc);
	public TaskCats searchCats(String taskId, String cattype, double lower, double upper);
	public TaskHist searchHist(String taskId, List<Integer> categories);
}
