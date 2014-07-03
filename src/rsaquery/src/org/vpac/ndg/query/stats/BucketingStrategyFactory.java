package org.vpac.ndg.query.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.Reflection;

public class BucketingStrategyFactory {

	static final Pattern URL = Pattern.compile("(\\w+)\\??(.*)");
	static final Pattern PARAM = Pattern.compile("(\\w+)=([^&]*)");

	BucketingStrategy create(String descriptor) throws QueryConfigurationException {
		Matcher matcher = URL.matcher(descriptor);
		if (!matcher.matches()) {
			throw new QueryConfigurationException(
					"Could not parse bucketing strategy descriptor.");
		}

		String path = matcher.group(1);
		String query = matcher.group(2);

		BucketingStrategy bs;

		if (path.equals("categorical")) {
			bs = new BucketingStrategyCategorical();
		} else if (path.equals("log")) {
			bs = new BucketingStrategyLog();
		} else if (path.equals("logQuantile")) {
			bs = new BucketingStrategyLogQuantile();
		} else {
			throw new QueryConfigurationException(String.format(
					"No known bucketing strategy matches the path %s", path));
		}

		for (Entry<String, String> entry : parseQuery(query).entrySet()) {
			Reflection.setSimpleField(bs, entry.getKey(), entry.getValue());
		}

		return bs;
	}

	Map<String, String> parseQuery(String query) {
		Matcher matcher = PARAM.matcher(query);
		Map<String, String> map = new HashMap<String, String>();
		while (matcher.find()) {
			map.put(matcher.group(1), matcher.group(2));
		}
		return map;
	}
}
