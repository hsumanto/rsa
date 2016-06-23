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
 */

package org.vpac.ndg.task;

import java.util.Collection;

import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.exceptions.TaskException;
/**
 * This abstract class represents a command to be executed.
 * @author hsumanto
 *
 */
public abstract class BaseTask implements ITask {
	/** Task description */
	private String description;
	/** Job progress on the task */
	private JobProgress progress;
	/** Perform cleaning up on source during finalise */
	private boolean cleanupSource;
	/** Perform cleaning up on target during finalise */
	private boolean cleanupTarget;
	/** Perform checking on source during execution */
	private boolean checkSource;
	/** Weight indicating how much contribution this task has to a task pipeline*/
	private double progressWeight = 1.0;
	/**
	 * Construct a task using the specified description.
	 * @param description The given task description.
	 */
	public BaseTask(String description) {
		setDescription(description);
		// By default clean up source and target
		setCleanupSource(true);
		setCleanupTarget(true);
		// By default check source during execution
		setCheckSource(true);
	}

	public void execute(Collection<String> actionLog) throws TaskException {
		//implemented to maintain compatibility with previous ITask interface
		this.execute(actionLog, null);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setProgress(JobProgress progress) {
		this.progress = progress;
	}

	public JobProgress getProgress() {
		return progress;
	}

	public void setCleanupSource(boolean cleanupSource) {
		this.cleanupSource = cleanupSource;
	}

	public boolean isCleanupSource() {
		return cleanupSource;
	}

	public void setCleanupTarget(boolean cleanupTarget) {
		this.cleanupTarget = cleanupTarget;
	}

	public boolean isCleanupTarget() {
		return cleanupTarget;
	}

	public boolean isCheckSource() {
		return checkSource;
	}

	public void setCheckSource(boolean checkSource) {
		this.checkSource = checkSource;
	}

	public double getProgressWeight() {
		return progressWeight;
	}

	public void setProgressWeight(double progressWeight) {
		this.progressWeight = progressWeight;
	}
}
