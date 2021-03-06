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

package org.vpac.ndg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.vpac.ndg.storage.dao.DatasetDao;

@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration({"/spring/config/TestBeanLocations.xml"})
public class SpringTest extends AbstractJUnit4SpringContextTests {

	public SpringTest() {
	}
	
	@Test
	public void testGetSameBeanFromContext() {
		DatasetDao datasetDao = (DatasetDao) ApplicationContextProvider.getApplicationContext().getBean("datasetDao");
		DatasetDao datasetDao2 = (DatasetDao) ApplicationContextProvider.getApplicationContext().getBean("datasetDao");
		
		assertEquals(datasetDao, datasetDao2);
	}
}
