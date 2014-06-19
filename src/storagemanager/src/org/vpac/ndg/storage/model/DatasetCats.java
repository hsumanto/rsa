package org.vpac.ndg.storage.model;

import java.io.Serializable;

import org.vpac.ndg.query.stats.Cats;

/**
 * Datacats for importing dataset categries 
 * @author Jin Park
 */
public class DatasetCats implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String datasetId;
	private String timeSliceId;
	private String bandId;
	private String name;
	private Cats cats;
	
	public DatasetCats() {
	}
	
	public DatasetCats(String datasetId, String timeSliceId, String bandId, String name, Cats cats) {
		this.datasetId = datasetId;
		this.timeSliceId = timeSliceId;
		this.bandId = bandId;
		this.cats = cats;
		this.name = name;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	public String getTimeSliceId() {
		return timeSliceId;
	}

	public void setTimeSliceId(String timeSliceId) {
		this.timeSliceId = timeSliceId;
	}

	public String getBandId() {
		return bandId;
	}

	public void setBandId(String bandId) {
		this.bandId = bandId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
}
