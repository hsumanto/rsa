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

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test case to test if CmdClient is working properly.
 * @author hsumanto
 * @author Alex Fraser
 */
@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration({"file:resources/spring/beans/CmdClientBean.xml"})
public class InvalidInputTests extends ConsoleTest {

	@Test
	public void testImportEmptyTimeSliceId() {
		executeWithDoom("data", "import", " ", "Band1", "DummyInput.nc");
		assertTrue(errput.toString().contains("Timeslice ID not specified"));
	}

	@Test
	public void testImportEmptyBandId() {
		executeWithDoom("data", "import", "DummyTimeSliceId", " ", "DummyInput.nc");
		assertTrue(errput.toString().contains("Band ID not specified"));
	}

	@Test
	public void testImportEmptyFile() {
		executeWithDoom("data", "import", "DummyTimeSliceId", "DummyBandId", " ");
		assertTrue(errput.toString().contains("No source file"));
	}

	@Test
	public void testImportInvalidTimeSliceId() {
		executeWithDoom("data", "import", "DummyTimeSliceId", "DummyBandId", "../../data/small_landsat/LS7_ETM_095_082_20100116_B30.nc");
		assertTrue(errput.toString().contains("TimeSlice \"DummyTimeSliceId\" not found"));
	}

	@Test
	public void testExportEmptyDatasetId() {
		executeWithDoom("data", "export", " ");
		assertTrue(errput.toString().contains("Dataset ID not specified"));
	}

	@Test
	public void testExportInvalidDatasetId() {
		executeWithDoom("data", "export", "DummyDatasetId");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testListTimeSliceEmptyDatasetId() {
		executeWithDoom("timeslice", "list", " ");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testListTimeSliceInvalidDatasetId() {
		executeWithDoom("timeslice", "list", "DummyDatasetId");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testListBandEmptyDatasetId() {
		executeWithDoom("band", "list", " ");
		assertTrue(errput.toString().contains("Dataset not found"));
	}	

	@Test
	public void testListBandInvalidDatasetId() {
		executeWithDoom("band", "list", "DummyDatasetId");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testcreateDatasetEmptyDatasetId() {
		executeWithDoom("dataset", "create", " ", "100m");
		assertTrue(errput.toString().contains("Dataset name not specified"));
	}	

	@Test
	public void testcreateDatasetInvalidResolution() {
		executeWithDoom("dataset", "create", "DummyDatasetName", "Invalid_100m_resolution");
		assertTrue(errput.toString().contains("No matching resolution"));
	}

	@Test
	public void testCreateTimeSliceEmptyDatasetId() {
		executeWithDoom("timeslice", "create", " ", "20120903");
		assertTrue(errput.toString().contains("Dataset ID not specified"));
	}

	@Test
	public void testCreateTimeSliceEmptyCreationDate() {
		executeWithDoom("timeslice", "create", "DummyDatasetId", " ");
		assertTrue(errput.toString().contains("Creation date not specified"));
	}

	@Test
	public void testCreateTimeSliceInvalidCreationDate() {
		executeWithDoom("timeslice", "create", "DummyDatasetId", "DummyDate");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testCreateBandEmptyDatasetId() {
		executeWithDoom("band", "create", " ", "DummyBand", "BYTE");
		assertTrue(errput.toString().contains("Dataset ID not specified"));
	}

	@Test
	public void testCreateBandInvalidDatasetId() {
		executeWithDoom("band", "create", "DummyDatasetId", "DummyBand", "BYTE");
		assertTrue(errput.toString().contains("Dataset not found"));
	}

	@Test
	public void testCreateBandEmptyBandName() {
		executeWithDoom("band", "create", "DummyDatasetId", " ", "BYTE");
		assertTrue(errput.toString().contains("Band name not specified"));
	}

	@Test
	public void testCreateBandEmptyDataType() {
		executeWithDoom("band", "create", "DummyDatasetId", "Band1", "--type", " ");
		assertTrue(errput.toString().contains("Dataset not found."));
	}
}
