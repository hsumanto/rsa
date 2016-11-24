package org.vpac.worker;

import java.io.Serializable;
import java.util.List;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.query.stats.VectorCats;
import org.vpac.worker.Job.WorkInfo;

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

	public static final class Fold implements Serializable {
		private static final long serialVersionUID = 1L;
		/** The name of the filter that generated the data */
		public WorkInfo currentWorkInfo;
		public final List<WorkInfo> list;

		public Fold(List<WorkInfo> list, WorkInfo currentWorkInfo) {
			this.list = list;
			this.currentWorkInfo = currentWorkInfo;
		}

		@Override
		public String toString() {
			return "Fold{" + "list=" + list
					+ ", currentWorkInfo=" + currentWorkInfo
					+ '}';
		}
	}
}
