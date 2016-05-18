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
 * Copyright 2016 VPAC Innovations
 */

package org.vpac.ndg.storage.model;

import java.io.Serializable;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.model.JobProgress;

/**
 * Associates a ledger with a [query] task.
 * @author Alex Fraser
 */
public class TaskLedger implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private JobProgress job;
	private CellSize outputResolution;
	private Ledger ledger;

	public TaskLedger() {
	}

	public TaskLedger(JobProgress job, CellSize outputResolution, Ledger ledger) {
		this.job = job;
		this.ledger = ledger;
		this.outputResolution = outputResolution;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JobProgress getJob() {
		return job;
	}

	public void setJob(JobProgress job) {
		this.job = job;
	}

	public Ledger getLedger() {
		return ledger;
	}

	public void setLedger(Ledger ledger) {
		this.ledger = ledger;
	}

	public CellSize getOutputResolution() {
		return outputResolution;
	}

	public void setOutputResolution(CellSize outputResolution) {
		this.outputResolution = outputResolution;
	}

}
