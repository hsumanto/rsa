package org.vpac.worker.master;

import java.io.Serializable;

public final class WorkResult implements Serializable {
	private static final long serialVersionUID = 1L;
	public final String workId;
    public final Object result;

    public WorkResult(String workId, Object result) {
      this.workId = workId;
      this.result = result;
    }

    @Override
    public String toString() {
      return "WorkResult{" +
        "workId='" + workId + '\'' +
        ", result=" + result +
        '}';
    }
  }