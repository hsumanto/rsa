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

package org.vpac.ndg.query;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.query.filter.Foldable;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;

/**
 * Command line application for running queries.
 * @author Alex Fraser
 */
public class QueryRunner {

	static final Logger log = LoggerFactory.getLogger(QueryRunner.class);

	public static Map<String, Foldable<?>> run(File config, File output) throws Exception {
		return run(config, output, 1);
	}

	public static Map<String, Foldable<?>> run(File config, File output,
			int threads) throws Exception {
		QueryDefinition qd = QueryDefinition.fromXML(config);
		File projectRoot = config.getParentFile();
		if (projectRoot == null)
			projectRoot = new File(".");
		return run(qd, projectRoot, output, threads);
	}

	/**
	 * Run a query from XML.
	 *
	 * @return {@link Query#getAccumulatedOutput() Accumulated metadata}. Note
	 *         that primary data is written to the output file.
	 */
	public static Map<String, Foldable<?>> run(QueryDefinition qd,
			File projectRoot, File output, int threads) throws Exception {

		log.debug("Opening output file {}", output);

		NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(
				Version.netcdf4_classic, output.getAbsolutePath());

		try {
			Query q = new Query(outputDataset);
			q.setNumThreads(threads);
			q.setMemento(qd, projectRoot.getAbsolutePath());
			try {
				q.run();
				return q.getAccumulatedOutput();
			} finally {
				q.close();
			}
		} finally {
			// Just catch exceptions on close - otherwise real exceptions get
			// hidden.
			// FIXME : Need to request to fix NetCdf java library 4.3.16 -
			// currently throws null pointer exception
			try {
				if (!outputDataset.isDefineMode())
					outputDataset.close();
			} catch (Exception e) {
				log.error("Could not close output dataset", e);
			}
		}
	}

	private static final String USAGE =
			"Usage: rsaquery <config file> <output file>";

	/**
	 * This class may be used to execute arbitrary queries from the command
	 * line. Accepts one argument: the file path of the query definition as XML.
	 */
	public static void main(String[] args) {
		File config;
		File output;
		try {
			config = new File(args[0]);
		} catch (IndexOutOfBoundsException e) {
			System.err.println(USAGE);
			System.err.println(
					"Please provide the path to a query defintion file.");
			System.exit(1);
			return;
		}
		try {
			output = new File(args[1]);
		} catch (IndexOutOfBoundsException e) {
			System.err.println(USAGE);
			System.err.println("Please provide the path to an output dataset.");
			System.exit(1);
			return;
		}

		try {
			run(config, output);
			System.out.println("Query finished.");
		} catch (QueryException e) {
			System.err.println("Configuration error: " + e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Query failed: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Query failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
	}
}
