package org.vpac.ndg.geometry;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test to ensure Projection method works correctly.
 * 
 * @author hsumanto
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration({"file:resources/spring/config/TestBeanLocations.xml"})
public class ProjectionTest extends TestCase {
	/**
	 * Test to verify that the specified EPSG code is in Geographic coordinate
	 * system.
	 */
	@Test
	public void testIsEpsgCode4326Geographic() {
		boolean bActualResult = Projection.isEpsgIdGeographic(4283);

		assertTrue(bActualResult);
	}

	/**
	 * Test to verify that the specified EPSG code is in Projected coordinate
	 * system.
	 */
	@Test
	public void testIsEpsgCode3112Geographic() {
		boolean bActualResult = Projection.isEpsgIdGeographic(3112);
		assertFalse(bActualResult);
	}

	/**
	 * Test geographic to projected transformation returns the same result. 4283
	 * -> 3112 == 4326 -> 3112
	 */
	@Test
	public void testGeographicToProjectedTransformation() {
		Point<Double> in = new Point<Double>(90.0, 0.0);
		int sourceEpsgId = 4283;
		int targetEpsgId = 3112;
		Point<Double> transformPt = Projection.transform(in, sourceEpsgId,
				targetEpsgId);
		sourceEpsgId = 4326;
		targetEpsgId = 3112;
		Point<Double> transformPt2 = Projection.transform(in, sourceEpsgId,
				targetEpsgId);
		double FLOAT_EPSILON = 0.0001;
		assertEquals(transformPt.getX(), transformPt2.getX(), FLOAT_EPSILON);
		assertEquals(transformPt.getY(), transformPt2.getY(), FLOAT_EPSILON);
	}
}