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

import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.TaskLedger;

public interface StatisticsDao {
	void saveCats(TaskCats tc);
	void saveOrReplaceCats(TaskCats tc);
	List<TaskCats> searchCats(String taskId, String cattype);

	void saveCats(DatasetCats dc);
	void saveOrReplaceCats(DatasetCats dc);
	List<DatasetCats> searchCats(String datasetId, String timeSliceId, String bandId, String catType);

	void save(Ledger l);
	void saveLedger(TaskLedger tl);
	void saveOrReplaceLedger(TaskLedger tl);
	Ledger getLedger(String ledgerId);
	TaskLedger getTaskLedger(String jobId);
}
