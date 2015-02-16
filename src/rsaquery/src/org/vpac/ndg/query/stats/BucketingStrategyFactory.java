package org.vpac.ndg.query.stats;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vpac.ndg.query.QueryBindingException;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.QueryRuntimeException;
import org.vpac.ndg.query.Reflection;

public class BucketingStrategyFactory {

	static final Pattern URL = Pattern.compile("(\\w+)\\??(.*)");
	static final Pattern PARAM = Pattern.compile("(\\w+)=([^&]*)");

	BucketingStrategy create(String descriptor) throws QueryException {
		Matcher matcher = URL.matcher(descriptor);
		if (!matcher.matches()) {
			throw new QueryBindingException(
					"Could not parse bucketing strategy descriptor.");
		}

		String path = matcher.group(1);
		String query = matcher.group(2);

		BucketingStrategy bs;

		if (path.equals("categorical")) {
			bs = new BucketingStrategyCategorical();
		} else if (path.equals("log")) {
			bs = new BucketingStrategyLog();
	    } else if (path.equals("explicit")) {
	        bs = new BucketingStrategyExplicit();
	    } else if (path.equals("regular")) {
	        bs = new BucketingStrategyRegular();
		} else if (path.equals("logRegular") || path.equals("logQuantile")) {
			// "logQuantile" is deprecated, but needed for compatibility until
			// client apps switch to "logRegular".
			bs = new BucketingStrategyLogRegular();
		} else {
			throw new QueryBindingException(String.format(
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
			String name;
			String value;
			try {
				name = URLDecoder.decode(matcher.group(1), "UTF-8");
				value = URLDecoder.decode(matcher.group(2), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new QueryRuntimeException("UTF-8 is not a recognised"
						+ " encoding. Can't decode parameters.");
			}
			map.put(name, value);
		}
		return map;
	}
}
