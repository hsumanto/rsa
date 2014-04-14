package org.vpac.worker.master;

import java.io.Serializable;

public class Ack implements Serializable {

	private static final long serialVersionUID = 1L;
	final String workId;
	
	public Ack(String workId) {
		this.workId = workId;
	}
	
	public String getWorkId() {
		return this.workId;
	}
	
	@Override
	public String toString() {
		return "Ack{" +
			"workId='" + workId + "\'" +
			"}";
	}
}
