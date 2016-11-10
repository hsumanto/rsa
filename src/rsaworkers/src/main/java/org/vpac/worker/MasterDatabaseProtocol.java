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

	public static final class SaveCats implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		/** The name of the filter that generated the data */
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

	public static final class SaveLedger implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		/** The name of the filter that generated the data */
		public final String key;
		public final CellSize resolution;
		public final Ledger ledger;

		public SaveLedger(String jobId, String key, CellSize resolution,
				Ledger ledger) {
			this.jobId = jobId;
			this.key = key;
			this.resolution = resolution;
			this.ledger = ledger;
		}

		@Override
		public String toString() {
			return String.format(
				"SaveLedger{jobId=%s, resolution=%s, ledger=%s}",
				jobId, resolution, ledger);
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
