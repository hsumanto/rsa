/*
 * This file is part of SpatialCube.
 *
 * SpatialCube is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SpatialCube is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SpatialCube.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.web.model.request;

import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.common.datamodel.TaskType;

import com.sun.istack.Nullable;

public class TaskSearchRequest {

	@Nullable
	private TaskType searchType;
	@Nullable
	private TaskState searchState;
	
	public TaskState getSearchState() {
		return searchState;
	}

	public void setSearchState(TaskState searchState) {
		this.searchState = searchState;
	}

	public TaskSearchRequest() {
	}

	public TaskSearchRequest(TaskType searchType, TaskState state) {
		this.setSearchType(searchType);
		this.setSearchState(state);
	}

	public TaskType getSearchType() {
		return searchType;
	}

	public void setSearchType(TaskType searchType) {
		this.searchType = searchType;
	}
}
