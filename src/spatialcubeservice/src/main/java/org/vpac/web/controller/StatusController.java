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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;

import org.vpac.actor.ActorCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;
import org.vpac.web.util.ControllerHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

@Controller
@RequestMapping("/Status")
@Transactional
public class StatusController {

	private Logger log = LoggerFactory.getLogger(StatusController.class);
    @Autowired
	private SessionFactory sessionFactory;

	@RequestMapping(value = "/postgres", method = RequestMethod.GET)
    @ResponseBody
	public String checkPostgres() {
        Session session = sessionFactory.getCurrentSession();
        return "OK";
	}

	@RequestMapping(value = "/{role}", method = RequestMethod.GET)
    @ResponseBody
	public String checkRole(@PathVariable String role) {
        log.info("Cluster");
		ActorSystem system = ActorCreator.getActorSystem();
        Cluster cluster = Cluster.get(system);
        List<Member> list = new ArrayList<Member>();
        for(Member m : cluster.state().getMembers()) {
            if (cluster.state().getSeenBy().contains(m.address()) && m.getRoles().contains(role))
                list.add(m);
        }
        return list.toString();
	}

}