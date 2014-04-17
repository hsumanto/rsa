package org.vpac.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.query.Query;
import org.vpac.ndg.query.QueryConfigurationException;
import org.vpac.ndg.query.QueryDefinition;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.ndg.query.stats.VectorHist;
import org.vpac.ndg.query.stats.dao.StatisticsDao;
import org.vpac.ndg.query.stats.dao.StatisticsDaoImpl;
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
	 * here is passed automatically to {@link AppContext} in the Storage
	 * Manager for use by other parts of the RSA.
	 */	
	private static enum AppContextSingleton {
		INSTANCE;

		public ApplicationContext appContext;

		private AppContextSingleton() {
			System.out.println("Path");
			appContext = new ClassPathXmlApplicationContext(
					new String[] {"spring/config/BeanLocations.xml"});
			
		}
	}
	
	public WorkExecutor() {
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;
		statisticsDao = (StatisticsDao)appContext.getBean("statisticsDao");		
	}
	
	StatisticsDao statisticsDao;	
	
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Integer) {
      Integer n = (Integer) message;
      int n2 = n.intValue() * n.intValue();
      String result = n + " * " + n + " = " + n2;
      log.debug("Produced result {}", result);
      getSender().tell(new Job.WorkComplete(result), getSelf());
    } else if (message instanceof Work) {
    	Work work = (Work) message;
    	String result = null;

    	final QueryDefinition qd = QueryDefinition.fromString(work.queryDefinitionString);
    	qd.output.grid.bounds = String.format("%f %f %f %f", work.bound.getMin().getX(), work.bound.getMin().getY(), work.bound.getMax().getX(), work.bound.getMax().getY());
		final WorkProgress wp = new WorkProgress(work.workId);
		Path outputDir = Paths.get("output/" + work.workId + "/");
		Map<String, Foldable<?>> output = null;
		
		final Path queryPath = outputDir.resolve("out.nc");
		if (!Files.exists(outputDir))
			try {
				Files.createDirectories(outputDir);
			} catch (IOException e1) {
				log.error("directory creation error:", e1);
				e1.printStackTrace();
				throw e1;
			}

		try {
			output = executeQuery(qd, wp, queryPath, work.netcdfVersion);
		} catch (Exception e) {
			wp.setErrorMessage(e.getMessage());
			log.error("Task exited abnormally: ", e);
			throw e;
		}
        
        log.debug("Produced result {}", output);
        getSender().tell(new Job.WorkComplete(result), getSelf());
    }
  }
	private Map<String, Foldable<?>> executeQuery(QueryDefinition qd, WorkProgress wp,
			Path outputPath, Version netcdfVersion)
			throws IOException, QueryConfigurationException {
		NetcdfFileWriter outputDataset = NetcdfFileWriter.createNew(netcdfVersion, outputPath.toString());
	
		Map<String, Foldable<?>> output = null;
				
		try {
			Query q = new Query(outputDataset);
			q.setNumThreads(1);
			q.setMemento(qd, "preview:");
			try {
				q.setProgress(wp);
				q.run();
				output = q.getAccumulatedOutput();
				VectorHist vh = (VectorHist)output.get("hist");
				statisticsDao.saveHist(vh.getComponents()[0]);
				System.out.println("output" + output);
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
}
