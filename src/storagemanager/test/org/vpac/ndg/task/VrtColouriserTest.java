package org.vpac.ndg.task;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration({"file:resources/spring/config/TestBeanLocations.xml"})
public class VrtColouriserTest extends AbstractJUnit4SpringContextTests {

	static final double EPSILON = 1.0e-17;

	@Test
	public void testHash() {
		// These need to match the hashes produced by any client that wishes
		// to use the same colour palette.
		VrtColouriser vc = new VrtColouriser();

		// Some small numbers
		assertEquals(0.9373889267444611, vc.hash(0), EPSILON);
		assertEquals(0.6515068486332893, vc.hash(1), EPSILON);
		assertEquals(0.5210794918239117, vc.hash(2), EPSILON);
		assertEquals(0.19616883620619774, vc.hash(3), EPSILON);
		assertEquals(0.5866535194218159, vc.hash(254), EPSILON);
		assertEquals(0.302638653665781, vc.hash(255), EPSILON);

		// Some prime numbers
		assertEquals(0.24170684441924095, vc.hash(562909), EPSILON);
		assertEquals(0.25825226679444313, vc.hash(5874277), EPSILON);
		assertEquals(0.2594611570239067, vc.hash(10236437), EPSILON);
		assertEquals(0.9379077218472958, vc.hash(15485497), EPSILON);

		// Some large numbers
		assertEquals(0.3471171371638775, vc.hash(0x7fffffff), EPSILON);
		assertEquals(0.6237552314996719, vc.hash(0xffffffff), EPSILON);
	}

	@Test
	public void testHashColour() {
		// These need to match the hashes produced by any client that wishes
		// to use the same colour palette.
		VrtColouriser vc = new VrtColouriser();

		// Some small numbers
		assertEquals("ffa6c927", Integer.toHexString(vc.hashColour(1).getRGB()));
		assertEquals("ff856577", Integer.toHexString(vc.hashColour(2).getRGB()));
		assertEquals("ff32381e", Integer.toHexString(vc.hashColour(3).getRGB()));
		assertEquals("ff962eec", Integer.toHexString(vc.hashColour(254).getRGB()));
		assertEquals("ff4d79ba", Integer.toHexString(vc.hashColour(255).getRGB()));

		// Some prime numbers
		assertEquals("ff3de07f", Integer.toHexString(vc.hashColour(562909).getRGB()));
		assertEquals("ff421cd2", Integer.toHexString(vc.hashColour(5874277).getRGB()));
		assertEquals("ff426c0b", Integer.toHexString(vc.hashColour(10236437).getRGB()));
		assertEquals("fff01ab8", Integer.toHexString(vc.hashColour(15485497).getRGB()));

		// Some large numbers
		assertEquals("ff58dcab", Integer.toHexString(vc.hashColour(0x7fffffff).getRGB()));
		assertEquals("ff9fae6c", Integer.toHexString(vc.hashColour(0xffffffff).getRGB()));
	}

}
