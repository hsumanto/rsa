package org.vpac.worker;

import java.io.Serializable;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.filter.Foldable;

public abstract class MasterDatabaseProtocol {

	public static final class JobUpdate implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		public final int workCompleted;
		public final int totalNoOfWork;
		
		public JobUpdate(String jobId, int workCompleted, int totalNoOfWork) {
			this.jobId = jobId;
			this.workCompleted = workCompleted;
			this.totalNoOfWork = totalNoOfWork;
		}

		@Override
		public String toString() {
			return "JobUpdate{" + "jobId=" + jobId + ",workCompleted=" + workCompleted + ",totalNoOfWork=" + totalNoOfWork + '}';
		}
	}

	public static final class SaveCats implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		public final String key;
		public final CellSize outputResolution;
		public final Foldable value;
		
		public SaveCats(String jobId, String key, CellSize outputResolution, Foldable value) {
			this.jobId = jobId;
			this.key = key;
			this.outputResolution = outputResolution;
			this.value = value;
		}

		@Override
		public String toString() {
			return "JobUpdate{" + "jobId=" + jobId + ",key=" + key + ",outputResolution=" + outputResolution.toHumanString() + '}';
		}
	}

	
}