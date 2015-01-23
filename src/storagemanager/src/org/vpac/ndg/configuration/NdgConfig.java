/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.ndg.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.geometry.Point;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This class is used to read NDG configuration from xml file.
 * @author hsumanto
 *
 */
@XStreamAlias("rsaConfig")
public class NdgConfig {
	// Projection parameters
	@XStreamAlias("srs")
	private int targetSrsEpsgId;

	@XStreamAlias("gridOrigin")
	private Point<Double> gridOriginPointInTargetSrs;

	public static class ResolutionSpec {
		@XStreamAsAttribute
		private CellSize cellSize;
		@XStreamAsAttribute
		private int tileSize;

		public CellSize getCellSize() {
			return cellSize;
		}
		public void setCellSize(CellSize cellSize) {
			this.cellSize = cellSize;
		}

		public int getTileSize() {
			return tileSize;
		}
		public void setTileSize(int tileSize) {
			this.tileSize = tileSize;
		}
	}

	@XStreamImplicit
	@XStreamAlias("resolution")
	private List<ResolutionSpec> resolutionList;
	private Map<CellSize, Integer> resolutionMap;

	@XStreamConverter(value=BooleanConverter.class)
	private boolean upPositive;

	@XStreamConverter(value=BooleanConverter.class)
	private boolean filelockingOn = true;
	// How often the process table is updated, in seconds. This is used to allow
	// locks to expire.
	private float heartBeatSpacing = 60.0f;
	// How many heart beats can be missed before a lock expires. Anything less
	// than 2 is likely to be dangerous.
	private int lockDeadline = 3;

	@XStreamConverter(value=BooleanConverter.class)
	private boolean generateImportTileAggregation;

	// Storage locations
	@XStreamAlias("storagepool")
	private String defaultStoragePool;
	@XStreamAlias("temploc")
	private String defaultTmpPool;
	@XStreamAlias("uploadloc")
	private String defaultUploadLocation;
	@XStreamAlias("pickuploc")
	private String defaultPickupLocation;
	@XStreamAlias("gdalprefix")
	private String gdalLocation;

	private String epiphanyHost;
	private String epiphanyPort;

	public String getEpiphanyHost() {
		return epiphanyHost;
	}

	public void setEpiphanyHost(String epiphanyHost) {
		this.epiphanyHost = epiphanyHost;
	}

	public String getEpiphanyPort() {
		return epiphanyPort;
	}

	public void setEpiphanyPort(String epiphanyPort) {
		this.epiphanyPort = epiphanyPort;
	}

	public String getTargetProjection() {
		return "EPSG:" + targetSrsEpsgId;
	}
	
	public int getTargetSrsEpsgId() {
		return targetSrsEpsgId;
	}
	
	public void setTargetSrsEpsgId(int targetEpsgId) {
		this.targetSrsEpsgId = targetEpsgId;
	}
	
	public List<ResolutionSpec> getResolutionList() {
		return resolutionList;
	}

	public Map<CellSize, Integer> getResolutionMap() {
		if (resolutionMap == null) {
			resolutionMap = new HashMap<>();
			for (ResolutionSpec res : resolutionList)
				resolutionMap.put(res.cellSize, res.tileSize);
		}
		return resolutionMap;
	}

	public void setResolutionList(List<ResolutionSpec> resolutionList) {
		this.resolutionList = resolutionList;
	}

	public Point<Double> getGridOriginPointInTargetSrs() {
		return gridOriginPointInTargetSrs;
	}

	public void setGridOriginPointInTargetSrs(
			Point<Double> gridOriginPointInTargetSrs) {
		this.gridOriginPointInTargetSrs = gridOriginPointInTargetSrs;
	}

	public String getDefaultStoragePool() {
		return defaultStoragePool;
	}

	public void setDefaultStoragePool(String defaultStoragePool) {
		this.defaultStoragePool = defaultStoragePool;
	}

	public String getDefaultTmpPool() {
		return defaultTmpPool;
	}

	public void setDefaultTmpPool(String defaultTmpPool) {
		this.defaultTmpPool = defaultTmpPool;
	}

	public String getDefaultUploadLocation() {
		return defaultUploadLocation;
	}

	public void setDefaultUploadLocation(String defaultUploadLocation) {
		this.defaultUploadLocation = defaultUploadLocation;
	}

	public String getDefaultPickupLocation() {
		return defaultPickupLocation;
	}

	public void setDefaultPickupLocation(String defaultPickupLocation) {
		this.defaultPickupLocation = defaultPickupLocation;
	}

	public String getGdalLocation() {
		return gdalLocation;
	}

	public void setGdalLocation(String gdalLocation) {
		this.gdalLocation = gdalLocation;
	}

	public boolean isUpPositive() {
		return upPositive;
	}

	public void setUpPositive(boolean upPositive) {
		this.upPositive = upPositive;
	}

	public boolean isFilelockingOn() {
		return filelockingOn;
	}

	public void setFilelockingOn(boolean filelockingOn) {
		this.filelockingOn = filelockingOn;
	}

	public boolean isGenerateImportTileAggregation() {
		return generateImportTileAggregation;
	}

	public void setGenerateImportTileAggregation(
			boolean generateImportTileAggregation) {
		this.generateImportTileAggregation = generateImportTileAggregation;
	}

	public float getHeartBeatSpacing() {
		return heartBeatSpacing;
	}

	public void setHeartBeatSpacing(float heartBeatSpacing) {
		this.heartBeatSpacing = heartBeatSpacing;
	}

	public int getLockDeadline() {
		return lockDeadline;
	}

	public void setLockDeadline(int lockDeadline) {
		this.lockDeadline = lockDeadline;
	}

}
