package org.vpac.ndg.query;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.stats.Statistics;

@RunWith(BlockJUnit4ClassRunner.class)
public class FilterUtilsTest extends TestCase {

	@Test
	public void test_filterList() {
		FilterUtils utils = new FilterUtils();
		Map<String, Class<? extends Filter>> filters = utils.getFilters();
		assertEquals("At least one filter", true, filters.size() > 0);
		System.out.println("Filters:");
		for (Class<?> c : filters.values())
			System.out.println("\t" + c.getName());
	}

	@Test
	public void test_filterFieldsList() {
		List<Field> fields;
		FilterUtils utils = new FilterUtils();

		fields = utils.getLiterals(Statistics.class);
		assertEquals(0, fields.size());

		fields = utils.getSources(Statistics.class);
		assertEquals(1, fields.size());
		assertEquals("input", fields.get(0).getName());

		fields = utils.getCells(Statistics.class);
		assertEquals(1, fields.size());
		assertEquals("output", fields.get(0).getName());
	}

}
