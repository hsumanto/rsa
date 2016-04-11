package org.vpac.ndg.colour;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.vpac.ndg.colour.HashPalette;
import org.vpac.ndg.colour.RangeMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/config/TestBeanLocations.xml" })
public class PaletteTest extends AbstractJUnit4SpringContextTests {

	static final double EPSILON = 1.0e-17;

	@Test
	public void testHash() {
		// These need to match the hashes produced by any client that wishes
		// to use the same colour palette.
		RangeMap rangeMap = new RangeMap(0, 1, 0, 1);
		HashPalette hp = new HashPalette(rangeMap);

		// Some small numbers
		assertEquals(0.9373889267444611, hp.hash(0), EPSILON);
		assertEquals(0.6515068486332893, hp.hash(1), EPSILON);
		assertEquals(0.5210794918239117, hp.hash(2), EPSILON);
		assertEquals(0.19616883620619774, hp.hash(3), EPSILON);
		assertEquals(0.5866535194218159, hp.hash(254), EPSILON);
		assertEquals(0.302638653665781, hp.hash(255), EPSILON);

		// Some prime numbers
		assertEquals(0.24170684441924095, hp.hash(562909), EPSILON);
		assertEquals(0.25825226679444313, hp.hash(5874277), EPSILON);
		assertEquals(0.2594611570239067, hp.hash(10236437), EPSILON);
		assertEquals(0.9379077218472958, hp.hash(15485497), EPSILON);

		// Some large numbers
		assertEquals(0.3471171371638775, hp.hash(0x7fffffff), EPSILON);
		assertEquals(0.6237552314996719, hp.hash(0xffffffff), EPSILON);
	}

	@Test
	public void testHashColour() {
		// These need to match the hashes produced by any client that wishes
		// to use the same colour palette.
		RangeMap rangeMap = new RangeMap(0, 1, 0, 1);
		HashPalette hp = new HashPalette(rangeMap);

		// Some small numbers
		assertEquals("ffa6c927", Integer.toHexString(hp.get(1).getRGB()));
		assertEquals("ff856577", Integer.toHexString(hp.get(2).getRGB()));
		assertEquals("ff32381e", Integer.toHexString(hp.get(3).getRGB()));
		assertEquals("ff962eec", Integer.toHexString(hp.get(254).getRGB()));
		assertEquals("ff4d79ba", Integer.toHexString(hp.get(255).getRGB()));

		// Some prime numbers
		assertEquals("ff3de07f", Integer.toHexString(hp.get(562909).getRGB()));
		assertEquals("ff421cd2", Integer.toHexString(hp.get(5874277).getRGB()));
		assertEquals("ff426c0b", Integer.toHexString(hp.get(10236437).getRGB()));
		assertEquals("fff01ab8", Integer.toHexString(hp.get(15485497).getRGB()));

		// Some large numbers
		assertEquals("ff58dcab", Integer.toHexString(hp.get(0x7fffffff).getRGB()));
		assertEquals("ff9fae6c", Integer.toHexString(hp.get(0xffffffff).getRGB()));
	}
}
