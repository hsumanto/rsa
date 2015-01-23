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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.exceptions.NdgConfigException;
import org.vpac.ndg.geometry.Box;
import org.vpac.ndg.geometry.Point;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class NdgConfigManager {

	final private Logger log = LoggerFactory.getLogger(NdgConfigManager.class);

	private NdgConfig config;

	public NdgConfig getConfig() {
		return config;
	}

	public void setConfig(NdgConfig config) {
		this.config = config;
	}

	public NdgConfigManager() {
	}

	/**
	 * Reads the NDG configuration from the xml file.
	 * @param xmlConfigFile The specified xml file.
	 * @return Returns the NDG configuration.
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unused")
	private void read(String xmlConfigFile) throws NdgConfigException {
		try {
			XStream xstream = createSerialiser();
			FileInputStream fis = new FileInputStream(xmlConfigFile);
			config = (NdgConfig) xstream.fromXML(fis);
		} catch(Exception ex) {
			throw new NdgConfigException(ex.getMessage(), ex);
		}
	}

	private void read(InputStream is) throws NdgConfigException {
		try {
			XStream xstream = createSerialiser();
			config = (NdgConfig) xstream.fromXML(is);
		} catch(Exception ex) {
			throw new NdgConfigException(ex.getMessage(), ex);
		}
	}

	public void write(String xmlConfigFile, NdgConfig config) throws FileNotFoundException {
		XStream xstream = createSerialiser();
		String xml = xstream.toXML(config);
		log.debug("{}", xml);
	}

	private XStream createSerialiser() {
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(NdgConfig.class);
		xstream.registerConverter(new PointConverter());
		xstream.aliasAttribute(Box.class, "xMin", "xMin");
		xstream.aliasAttribute(Box.class, "xMax", "xMax");
		xstream.aliasAttribute(Box.class, "yMin", "yMin");
		xstream.aliasAttribute(Box.class, "yMax", "yMax");
		return xstream;
	}

	public void configure() throws NdgConfigException, IOException {
		if(config == null) {
			String file = "/rsa.xml";
			InputStream inputStream = NdgConfig.class.getResourceAsStream(file);
			if (inputStream == null)
				throw new NdgConfigException("Could not find rsa.xml");
			read(inputStream);
		}
	}

	public static class PointConverter implements Converter {
		@Override
		@SuppressWarnings("rawtypes")
		public boolean canConvert(Class c) {
			return c.equals(Point.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Point<Double> point = (Point<Double>) source;
			writer.addAttribute("x", point.getX().toString());
			writer.addAttribute("y", point.getY().toString());
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			Point<Double> point = new Point<>();
			point.setX(Double.parseDouble(reader.getAttribute("x")));
			point.setY(Double.parseDouble(reader.getAttribute("y")));
			return point;
		}
	}

}
