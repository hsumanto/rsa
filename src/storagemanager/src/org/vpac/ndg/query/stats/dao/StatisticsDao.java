package org.vpac.ndg.query.stats.dao;

import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;

public interface StatisticsDao {
	public void saveHist(Hist h);
	public void saveCats(Cats c);
}
