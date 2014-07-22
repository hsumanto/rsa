package org.vpac.ndg.query;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.vpac.ndg.query.filter.Description;
import org.vpac.ndg.query.filter.Filter;
import org.vpac.ndg.query.sampling.Cell;
import org.vpac.ndg.query.sampling.PixelSource;

/**
 * Utilities for interrogating available filters.
 *
 * <p>
 * To be available, filters must be registered by having a
 * <em>filter-pack.cfg</em> file in their package directory. The contents of
 * that file do not matter.
 * </p>
 *
 * @author Alex Fraser
 * @author hsumanto
 */
public class FilterUtils {

	/**
	 * @return A collection of the filters that are currently available. The
	 *         process of gathering this information is slow (600ms or so).
	 *         Consider caching the results.
	 */
	public Map<String, Class<? extends Filter>> getFilters() {

		// First scan: find packages that contain the filter-pack.cfg file.
		Configuration config = new ConfigurationBuilder()
				.filterInputsBy(new FilterBuilder()
						.exclude("java.*")
						.exclude("sun.*"))
				.setUrls(ClasspathHelper.forClassLoader())
				.setScanners(new ResourcesScanner());
		Reflections refl = new Reflections(config);
		Set<String> res = refl.getResources(Pattern.compile("filter-pack.cfg"));

		// Second scan: find classes in those packages that implement Filter.
		Map<String, Class<? extends Filter>> filters =
				new HashMap<String, Class<? extends Filter>>();
		for (String path : res) {
			String pack = path.substring(0, path.lastIndexOf("/"))
					.replace("/", ".");
			Reflections reflections = new Reflections(pack);
			for (Class<? extends Filter> clazz : reflections.getSubTypesOf(Filter.class)) {
				if (Modifier.isAbstract(clazz.getModifiers()))
					continue;
				filters.put(clazz.getName(), clazz);
			}
		}
		return filters;
	}

	public String getName(Class<? extends Filter> clazz) {
		Description annotation = clazz.getAnnotation(Description.class);
		if (annotation != null)
			return annotation.name();
		else
			return clazz.getSimpleName();
	}

	public String getDescription(Class<? extends Filter> clazz) {
		Description annotation = clazz.getAnnotation(Description.class);
		if (annotation != null)
			return annotation.description();
		else
			return "";
	}

	/**
	 * @param clazz The filter class to inspect.
	 * @return A list of fields that can have literal values (primitives)
	 *         assigned to them.
	 */
	public List<Field> getLiterals(Class<? extends Filter> clazz) {
		List<Field> res = new ArrayList<Field>();
		for (Field f : clazz.getFields()) {
			if (!Modifier.isPublic(f.getModifiers()))
				continue;
			Class<?> cls = f.getType();
			if (cls.isPrimitive() || String.class.isAssignableFrom(cls))
				res.add(f);
		}
		return res;
	}

	/**
	 * @param clazz The filter class to inspect.
	 * @return A list of fields that can have {@link PixelSource}s assigned to
	 *         them.
	 */
	public List<Field> getSources(Class<? extends Filter> clazz) {
		List<Field> res = new ArrayList<Field>();
		for (Field f : clazz.getFields()) {
			if (!Modifier.isPublic(f.getModifiers()))
				continue;
			if (f.getType().isPrimitive())
				continue;
			if (PixelSource.class.isAssignableFrom(f.getType()))
				res.add(f);
		}
		return res;
	}

	/**
	 * @param clazz The filter class to inspect.
	 * @return A list of fields that can have {@link Cell}s assigned to them.
	 */
	public List<Field> getCells(Class<? extends Filter> clazz) {
		List<Field> res = new ArrayList<Field>();
		for (Field f : clazz.getFields()) {
			if (!Modifier.isPublic(f.getModifiers()))
				continue;
			if (f.getType().isPrimitive())
				continue;
			if (Cell.class.isAssignableFrom(f.getType()))
				res.add(f);
		}
		return res;
	}
}