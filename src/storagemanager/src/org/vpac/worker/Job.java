package org.vpac.worker;

import java.io.Serializable;

import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.task.Task;

import ucar.nc2.NetcdfFileWriter.Version;

public class Job {

 public static final class Work implements Serializable {
	private static final long serialVersionUID = 1L;
	public final String workId;
    //public final Object job;
    public final String queryDefinitionString;
//    public final QueryProgress qp;
    public final Version netcdfVersion;
    public final BoxReal bound;
    public final String jobProgressId;

    public Work(String workId, String queryDefinitionString, Version ver, BoxReal bound, String jobProgressId) {
      this.workId = workId;
      this.queryDefinitionString = queryDefinitionString;
      this.netcdfVersion = ver;
      this.jobProgressId = jobProgressId;
      this.bound = bound;
      //this.job = job;
    }

    @Override
    public String toString() {
      return "Work{" +
        "workId='" + workId + '\'' +
        ", qd=" + queryDefinitionString +
        ", ver=" + netcdfVersion +
        ", job=" + jobProgressId +
        ", bound=" + bound +
        '}';
    }
 
 }
 
 public static final class WorkComplete  implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Object result;

    public WorkComplete(Object result) {
      this.result = result;
    }

    @Override
    public String toString() {
      return "WorkComplete{" +
        "result=" + result +
        '}';
    }
  }
}
