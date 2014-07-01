package org.vpac.worker;

import java.io.Serializable;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.filter.Foldable;

public abstract class MasterDatabaseProtocol {

	public static final class JobUpdate implements Serializable {
		private static final long serialVersionUID = 1L;
		public final String jobId;
		public final double completedArea;
		public final double totalArea;
		
		public JobUpdate(String jobId, double  completedArea, double totalArea) {
			this.jobId = jobId;
			this.completedArea = completedArea;
			this.totalArea = totalArea;
		}

		@Override
		public String toString() {
			return "JobUpdate{" + "jobId=" + jobId + ",completedArea=" + completedArea + ",totalArea=" + totalArea + '}';
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