package org.vpac.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.AppContext;
import org.vpac.ndg.configuration.NdgConfigManager;
import org.vpac.ndg.query.Query;
import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.QueryDefinition.DatasetInputDefinition;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.stats.VectorHist;
import org.vpac.ndg.query.stats.dao.StatisticsDao;
import org.vpac.worker.Job.Work;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class WorkExecutor extends UntypedActor {

	/**
	 * The application context should only be initialised once EVER - otherwise
	 * you get resource leaks (e.g. extra open sockets) when using something
	 * like Nailgun. The use of the enum here ensures this. The context acquired
	 * here is passed automatically to {@link AppContext} in the Storage Manager
	 * for use by other parts of the RSA.
	 */
	private static enum AppContextSingleton {
		INSTANCE;

		public ApplicationContext appContext;

		private AppContextSingleton() {
			System.out.println("Path");
			appContext = new ClassPathXmlApplicationContext(
					new String[] { "spring/config/BeanLocations.xml" });

		}
	}

	public WorkExecutor() {
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;
		ndgConfigManager = (NdgConfigManager) appContext
				.getBean("ndgConfigManager");
	}

	private NdgConfigManager ndgConfigManager;
	// Dataset identifier looks like "epiphany:id"
	private static final Pattern DATASET_PATTERN = Pattern.compile("^([^/]+)");

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof Work) {
			Work work = (Work) message;
			WorkProgress wp = new WorkProgress(work.workId);
			Collection<Path> tempFiles = new ArrayList<>();

			Map<String, Foldable<?>> output;
			try {
				QueryDefinition qd = preprocessQueryDef(work, tempFiles);
				Path queryPath = getOutputPath(work);
				output = executeQuery(qd, wp, queryPath, work.netcdfVersion);
			} catch (Exception e) {
				wp.setErrorMessage(e.getMessage());
				log.error(e, "Task {} exited abnormally", work.workId);
				throw e;
			} finally {
				for (Path path : tempFiles) {
					try {
						Files.delete(path);
					} catch (IOException e) {
						log.error(e, "Failed to delete temp file {}", path);
					}
				}
			}
			HashMap<String, Foldable<? extends Serializable>> result = new java.util.HashMap<>();
			for(Entry<String, ?> v : output.entrySet()) {
				if(Serializable.class.isAssignableFrom(v.getValue().getClass()))
					result.put(v.getKey(), (Foldable<? extends Serializable>) v.getValue());
			}
			
			log.debug("Produced result {}", output);
			getSender().tell(new Job.WorkComplete(result), getSelf());
		}
	}

	private QueryDefinition preprocessQueryDef(Work work,
			Collection<Path> tempFiles) throws IOException {
		final QueryDefinition qd1 = QueryDefinition
				.fromString(work.queryDefinitionString);
		qd1.output.grid.bounds = String.format("%f %f %f %f", work.bound
				.getMin().getX(), work.bound.getMin().getY(), work.bound
				.getMax().getX(), work.bound.getMax().getY());

		// Fetch external data and store in local files before executing query.
		// This is required because the query engine needs to know some metadata
		// early in the query configuration process, before the files are
		// opened.
		for (DatasetInputDefinition di : qd1.inputs) {
			if (di.href.startsWith("epiphany")) {
				Path epiphanyTempFile = fetchEpiphanyData(di, work);
				tempFiles.add(epiphanyTempFile);
				di.href = epiphanyTempFile.toString();
			}
		}
		return qd1;
	}

	private Path getOutputPath(Work work) throws IOException {

		Path outputDir = Paths.get(ndgConfigManager.getConfig()
				.getDefaultPickupLocation() + "/" + work.jobProgressId);
		Path queryPath = outputDir.resolve(work.workId + "_out.nc");

		if (!Files.exists(outputDir))
			try {
				Files.createDirectories(outputDir);
			} catch (IOException e1) {
				log.error("directory creation error:", e1);
				e1.printStackTrace();
				throw e1;
			}
		return queryPath;
	}

	private Map<String, Foldable<?>> executeQuery(QueryDefinition qd,
			WorkProgress wp, Path outputPath, Version netcdfVersion)
			throws IOException, QueryConfigurationException {

		/*
		 * NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(
		 * netcdfVersion, outputPath.toString());
		 */
		NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(
				netcdfVersion, outputPath.toString());

		Map<String, Foldable<?>> output = null;

		try {
			Query q = new Query(outputDataset);
			q.setNumThreads(1);
			q.setMemento(qd, new File(".").getAbsolutePath());
			try {
				q.setProgress(wp);
				q.run();
				output = q.getAccumulatedOutput();
			} finally {
				q.close();
			}
		} finally {
			try {
				outputDataset.close();
			} catch (Exception e) {
				log.error("Failed to close output file", e);
			}
		}
		return output;
	}

	private Path fetchEpiphanyData(DatasetInputDefinition di, Work w)
			throws IOException {
		String epiphanyHost = ndgConfigManager.getConfig().getEpiphanyIp();
		String epiphanyPort = ndgConfigManager.getConfig().getEpiphanyPort();
		String query = di.href.contains("?") ? di.href.substring(di.href
				.indexOf("?") + 1) : "";
		String datasetId = findDataset(di.href);
		String url = "http://"
				+ epiphanyHost
				+ ":"
				+ epiphanyPort
				+ "/map/wcs/"
				+ datasetId
				+ "?LAYERS="
				+ datasetId
				+ "&FORMAT=application%2Fx-netCDF&SERVICE=WCS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&YEAR=none&QUERY=none&GEOMETRY=none&VIEWMETHOD=none&COLOURSCHEME=ColourBrewer%20Blues&LEGENDEXTENT=STATIC&NUMBERFILTERS=none&VISTYPE=none&SRS=EPSG%3A3111&BBOX="
				+ w.bound.getMin().getX() + "," + w.bound.getMin().getY() + ","
				+ w.bound.getMax().getX() + "," + w.bound.getMax().getY() + "&"
				+ query + "&WIDTH=5000&HEIGHT=5000";
		URL connectionUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) connectionUrl
				.openConnection();
		connection.setRequestMethod("GET");

		Path path = Files.createTempFile("epiphany_" + w.workId, ".nc");
		// TODO : file doesn't need to be stored. -JP
		// It's probably good practice to store it, though: otherwise we might
		// fill our RAM. rsaquery will only read a small amount at a time. -AF
		try (InputStream in = connection.getInputStream();
				OutputStream out = new FileOutputStream(path.toFile());) {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}
		return path;
	}

	private String findDataset(String uri) throws IOException {

		URI parsedUri;
		try {
			parsedUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new IOException("Could not open dataset", e);
		}

		String path = parsedUri.getSchemeSpecificPart();
		Matcher matcher = DATASET_PATTERN.matcher(path);
		if (!matcher.matches()) {
			throw new FileNotFoundException(String.format(
					"Invalid dataset name %s", path));
		}
		return matcher.group(1);
	}
}
