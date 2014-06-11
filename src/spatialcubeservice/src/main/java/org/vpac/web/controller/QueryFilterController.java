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

package org.vpac.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.vpac.ndg.query.FilterUtils;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.web.model.request.PagingRequest;
import org.vpac.web.model.response.QueryFilterResponse;
import org.vpac.web.model.response.QueryNodeCollectionResponse;
import org.vpac.web.util.ControllerHelper;

/**
 * This class is intended as controller for QueryFilter related web services.
 * 
 * @author hsumanto
 * 
 */
@Controller
@RequestMapping("/QueryFilter")
public class QueryFilterController {

	@Autowired
	ControllerHelper helper;

	private Collection<Class<? extends Filter>> filters;

	@InitBinder
	public void binder(WebDataBinder binder) {
		helper.BindDateTimeFormatter(binder);
		helper.BindCellSizeFormatter(binder);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getAllQueryFilter(
			@RequestParam(required = false) String name,
			@Valid PagingRequest page, ModelMap model) throws IOException {
 
		List<QueryFilterResponse> items = new ArrayList<>();
		for (Class<? extends Filter> f : getFilters()) {
			items.add(new QueryFilterResponse(f));
		}
		model.addAttribute(ControllerHelper.RESPONSE_ROOT,
				new QueryNodeCollectionResponse("Filters", items));
		return "List";
	}

	private Collection<Class<? extends Filter>> getFilters() {
		if (filters == null)
			filters = new FilterUtils().getFilters().values();
		return filters;
	}

}
