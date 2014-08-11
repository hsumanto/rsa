package org.vpac.worker;

import java.io.Serializable;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.query.stats.VectorCats;

public abstract class MasterDatabaseProtocol {

	public static final class JobUpdate implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		public final double fraction;
		public final TaskState state;
		public final String errorMessage;

		public JobUpdate(String jobId, double fraction, TaskState state, String errorMessage) {
			this.jobId = jobId;
			this.fraction = fraction;
			this.state = state;
			this.errorMessage = errorMessage;
		}

		@Override
		public String toString() {
			return "JobUpdate{" + "jobId=" + jobId + ",fraction=" + fraction
					+ ",state=" + state + '}';
		}
	}

	public static final class SaveCats implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		public final String key;
		public final CellSize outputResolution;
		public final VectorCats cats;

		public SaveCats(String jobId, String key, CellSize outputResolution,
				VectorCats cats) {
			this.jobId = jobId;
			this.key = key;
			this.outputResolution = outputResolution;
			this.cats = cats;
		}

		@Override
		public String toString() {
			return "SaveCats{" + "jobId=" + jobId + ",key=" + key
					+ ",outputResolution=" + outputResolution.toHumanString()
					+ '}';
		}
	}
}