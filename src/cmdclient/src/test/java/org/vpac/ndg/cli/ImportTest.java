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

package org.vpac.ndg.cli;

import static org.junit.Assert.assertNotNull;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

/**
 * Test case to test if CmdClient is working properly.
 * @author hsumanto
 * @author Alex Fraser
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/beans/CmdClientBean.xml"})
public class ImportTest extends ConsoleTest {
	@Rule
	public MethodRule benchmarkRun = new BenchmarkRule();

	@Test
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
	public void testImportTimeSlices() {
		DecimalFormat df = new DecimalFormat( "00" );
		for(int dateCount=1; dateCount<=3; dateCount++) {
			String dfDay = df.format(dateCount);
			String acquisitionTime = "2010-01-" + dfDay + "T01-50-55.000";
			String datasetName = "DummyDataset123";
			String bandName = "DummyBand123";
			String datasetResolution = "100m";
			String type = "BYTE";
			String primaryFileLocation = "../../data/small_landsat/LS7_ETM_095_082_20100116_B30.nc";

			// Assure data structures are in place.
			String datasetId = ensureDatasetExists(datasetName, datasetResolution);
			String tsId = ensureTimeSliceExists(datasetId, acquisitionTime);
			String bandId = ensureBandExists(datasetId, bandName, type);

			// Now the actual test!
			execute("data", "import", tsId, bandId, primaryFileLocation);
		}
	}

	@Test
	@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
	public void testImportTimeSlice() {
		String acquisitionTime = "2010-01-24T01-50-55.000";
		String datasetName = "DummyDataset123";
		String bandName = "DummyBand123";
		String datasetResolution = "100m";
		String type = "BYTE";
		String primaryFileLocation = "../../data/small_landsat/LS7_ETM_095_082_20100116_B30.nc";

		// Assure data structures are in place.
		String datasetId = ensureDatasetExists(datasetName, datasetResolution);
		String tsId = ensureTimeSliceExists(datasetId, acquisitionTime);
		String bandId = ensureBandExists(datasetId, bandName, type);

		// Now the actual test!
		execute("data", "import", tsId, bandId, primaryFileLocation);
	}

	protected String findId(String clientOutput, String value) {
		Pattern pattern = Pattern.compile("^([^ ]+) ([^ ]+).*");
		String id = null;
		for (String line : clientOutput.split("\n")) {
			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches())
				continue;
			if (!matcher.group(2).equals(value))
				continue;
			id = matcher.group(1);
			break;
		}
		return id;
	}

	protected String ensureDatasetExists(String name, String resolution) {
		// Check whether the time slice exists.
		String path = name + "/" + resolution;
		try {
			execute("dataset", "show", path);
		} catch (ClientExitException e) {
			// This is OK, it just means this is the first time we've run.
		}
		String dsId = findId(output.toString(), path);
		output.reset();
		errput.reset();

		if (dsId == null) {
			execute("dataset", "create", name, resolution);
			dsId = findId(output.toString(), path);
			output.reset();
			errput.reset();
		}
		assertNotNull("Failed to create dataset.", dsId);
		return dsId;
	}

	protected String ensureTimeSliceExists(String datasetId, String acquisitionTime) {
		// Check whether the time slice exists.
		try {
			execute("timeslice", "list", datasetId);
		} catch (ClientExitException e) {
			// This is OK, it just means this is the first time we've run.
		}
		String tsId = findId(output.toString(), acquisitionTime);
		output.reset();
		errput.reset();

		if (tsId == null) {
			execute("timeslice", "create", datasetId, acquisitionTime);
			tsId = findId(output.toString(), acquisitionTime);
			output.reset();
			errput.reset();
		}
		assertNotNull("Failed to create time slice.", tsId);
		return tsId;
	}

	protected String ensureBandExists(String datasetId, String bandName, String type) {
		// Check whether the band already exists.
		try {
			execute("band", "list", datasetId);
		} catch (ClientExitException e) {
			// This is OK, it just means this is the first time we've run.
		}
		String bandId = findId(output.toString(), bandName);
		output.reset();
		errput.reset();

		if (bandId == null) {
			execute("band", "create", datasetId, bandName, type);
			bandId = findId(output.toString(), bandName);
			output.reset();
			errput.reset();
		}
		assertNotNull("Failed to create band.", bandId);
		return bandId;
	}
}
