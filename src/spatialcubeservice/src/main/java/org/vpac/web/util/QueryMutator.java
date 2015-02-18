package org.vpac.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vpac.ndg.query.NodeReference;
import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.QueryDefinition.VariableDefinition;
import org.vpac.ndg.query.QueryException;
import org.vpac.ndg.query.Resolve;
import org.vpac.ndg.query.QueryDefinition.DatasetInputDefinition;
import org.vpac.ndg.query.QueryDefinition.FilterDefinition;
import org.vpac.ndg.query.QueryDefinition.LiteralDefinition;
import org.vpac.ndg.query.QueryDefinition.SamplerDefinition;
import org.vpac.ndg.query.stats.BucketingStrategy;

public class QueryMutator {

	private QueryDefinition qd;
	private Resolve resolve;
	private int id;

	public QueryMutator(QueryDefinition qd) {
		this.qd = qd;
		resolve = new Resolve();
		id = 0;
	}

	/**
	 * Insert a filter into a query chain.
	 *
	 * <p>
	 * Connects an output of fd to the target reference. The input reference
	 * that the target used to be connected to will be assigned to an input of
	 * fd.
	 * </p>
	 *
	 * @param fd The filter to insert.
	 * @param inputSocket The name of the input socket of fd to attach.
	 * @param targetRef A reference to the input socket that is being attached to.
	 * @param outputSocket The name of the output socket of fd to attach.
	 * @throws QueryException If the target can not be found.
	 */
	public void insert(FilterDefinition fd, String inputSocket,
			String targetRef, String outputSocket) throws QueryException {

		String sourceRef = null;
		String newRef = String.format("#%s/%s", fd.id, outputSocket);

		NodeReference nr = resolve.decompose(targetRef);
		boolean foundTarget = false;
		if (nr.getNodeId().equals(qd.output.id)) {
			for (VariableDefinition vd : qd.output.variables) {
				if (nr.getSocketName().equals(vd.name)) {
					sourceRef = vd.ref;
					vd.ref = newRef;
					foundTarget = true;
					break;
				}
			}
		} else {
			for (FilterDefinition fd2 : qd.filters) {
				if (!nr.getNodeId().equals(fd2.id))
					continue;
				for (SamplerDefinition sd : fd2.samplers) {
					if (nr.getSocketName().equals(sd.name)) {
						sourceRef = sd.ref;
						sd.ref = newRef;
						foundTarget = true;
						break;
					}
				}
			}
		}

		if (!foundTarget) {
			throw new QueryException(String.format(
					"Could not insert filter %s: target socket %s could not " +
					"be found.", fd.id, targetRef));
		}

		SamplerDefinition input = null;
		for (SamplerDefinition sd : fd.samplers) {
			if (inputSocket.equals(sd.name)) {
				input = sd;
				break;
			}
		}
		if (input == null) {
			input = new SamplerDefinition().name(inputSocket);
			fd.sampler(input);
		}
		input.ref = sourceRef;
	}

	static final Pattern GROUP_PATTERN = Pattern.compile("rsa:([^/]+)/([^/]+)/([^/]+)");

	/**
	 * Create and insert a categorisation filter.
	 *
	 * @param groupBy The band to group by, in the form 'rsa:dataset/res/band'
	 * @param buckets The {@link BucketingStrategy bucketing strategy} to use.
	 * @return The new filter.
	 */
	public FilterDefinition addCategoriser(String groupBy,
			String buckets) {

		if (qd.output.variables.size() != 1) {
			throw new IllegalArgumentException(
					"Can't automatically add categorisation filter: " +
					"output node must have exactly one input socket.");
		}

		Matcher matcher = GROUP_PATTERN.matcher(groupBy);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(String.format(
				"Can't group by %s: unrecognised URI.", groupBy));
		}

		String dataset = matcher.group(1);
		String resolution = matcher.group(2);
		String band = matcher.group(3);

		String originalSourceRef = qd.output.variables.get(0).ref;

		String inputHref = String.format("rsa:%s/%s", dataset, resolution);
		DatasetInputDefinition di = getOrAddInput(inputHref);

		FilterDefinition cat = new FilterDefinition()
			.id(band)
			.classname("org.vpac.ndg.query.stats.Categories")
			.literal(new LiteralDefinition()
				.name("buckets")
				.value(buckets))
			.sampler(new SamplerDefinition()
				.name("input")
				.ref(originalSourceRef))
			.sampler(new SamplerDefinition()
				.name("categories")
				.ref(String.format("#%s/%s", di.id, band)));

		qd.output.variables.get(0).ref = String.format("#%s/output", cat.id);

		return cat;
	}

	public DatasetInputDefinition getOrAddInput(String href) {
		for (DatasetInputDefinition di : qd.inputs) {
			if (href.equals(di.href))
				return di;
		}

		DatasetInputDefinition di = new DatasetInputDefinition()
			.id(String.format("autoinput_%d", id++))
			.href(href);
		qd.inputs.add(di);
		return di;
	}
}
