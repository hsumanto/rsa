package org.vpac.ndg.storage.model;

import java.io.Serializable;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Cats;

/**
 * Groups values into arbitrary buckets (categories to be provided by user). 
 * @author Jin Park
 */
public class TaskCats implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String taskId;
	private String name;
	private CellSize outputResolution;
	private Cats cats;
	
	public TaskCats() {
	}
	
	public TaskCats(String taskId, String name, CellSize outputResolution, Cats cats) {
		this.taskId = taskId;
		this.cats = cats;
		this.name = name;
		this.outputResolution = outputResolution;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Cats getCats() {
		return cats;
	}

	public void setCats(Cats cats) {
		this.cats = cats;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CellSize getOutputResolution() {
		return outputResolution;
	}

	public void setOutputResolution(CellSize outputResolution) {
		this.outputResolution = outputResolution;
	}
}
