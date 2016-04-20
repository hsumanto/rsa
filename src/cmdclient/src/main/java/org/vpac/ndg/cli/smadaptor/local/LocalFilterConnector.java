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

package org.vpac.ndg.cli.smadaptor.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vpac.ndg.cli.smadaptor.FilterConnector;
import org.vpac.ndg.query.FilterUtils;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.web.model.response.QueryFilterResponse;
import org.vpac.web.model.response.QueryNodeCollectionResponse;

public class LocalFilterConnector implements FilterConnector {

	@Override
	public QueryNodeCollectionResponse list() {
		FilterUtils util = new FilterUtils();
		Collection<Class<? extends Filter>> filters = util.getFilters().values();
		List<QueryFilterResponse> items = new ArrayList<>();
		for (Class<? extends Filter> f : filters) {
			items.add(new QueryFilterResponse(f));
		}
		return new QueryNodeCollectionResponse("Filters", items);
	}

}