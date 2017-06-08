package org.vpac.worker;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.AppContext;
import org.vpac.ndg.configuration.NdgConfigManager;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.VectorCats;
import org.vpac.ndg.query.stats.Ledger;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.ndg.storage.model.DatasetCats;
import org.vpac.ndg.storage.model.TaskLedger;
import org.vpac.worker.MasterDatabaseProtocol.JobUpdate;
import org.vpac.worker.MasterDatabaseProtocol.Fold;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.query.filter.Foldable;
import org.vpac.worker.Job.Work;
import org.vpac.worker.Job.WorkInfo;


public class DatabaseActor extends UntypedActor {

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
			appContext = new ClassPathXmlApplicationContext(
					new String[] { "spring/config/BeanLocations.xml" });

		}
	}

	private JobProgressDao jobProgressDao;
	private StatisticsDao statisticsDao;
	private NdgConfigManager ndgConfigManager;
	private final LoggingAdapter log = Logging.getLogger(getContext().system(),
		this);

	public DatabaseActor() {
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;
		statisticsDao = (StatisticsDao) appContext.getBean("statisticsDao");
		jobProgressDao = (JobProgressDao) appContext.getBean("jobProgressDao");
		ndgConfigManager = (NdgConfigManager) appContext.getBean("ndgConfigManager");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		log.info("message:" + message);
		if (message instanceof JobUpdate) {
			JobUpdate job = (JobUpdate) message;
			JobProgress progress = jobProgressDao.retrieve(job.jobId);
			progress.setErrorMessage(job.errorMessage);
			progress.setCurrentStepProgress(100 * job.fraction);
			progress.setState(job.state);
			if (job.state == TaskState.FINISHED)
				progress.setCompleted();
			jobProgressDao.save(progress);

		} else if (message instanceof Fold) {
			Fold fold = (Fold) message;
			foldResults(fold.currentWorkInfo, fold.list);

		} else if (message instanceof String) {
			System.out.println("message:" + message);
		}
	}

	private boolean isTaskCatsExist(String jobProgressId, String key) {
		List<TaskCats> tc = statisticsDao.searchCats(jobProgressId, key);
		if (tc.size() > 0)
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	private void foldResults(WorkInfo currentWorkInfo, List<String> list) {
		HashMap<String, Foldable<?>> resultMap = new HashMap<>();

		for (String w : list) {
			Map<String, Foldable<?>> map = DeserializeResult(currentWorkInfo.work.jobProgressId, w);
			for (Entry<String, ?> v : map.entrySet()) {
				Foldable<?> result;
				if (VectorCats.class.isAssignableFrom(v.getValue().getClass())) {
					VectorCats baseResult = (VectorCats) resultMap.get(v.getKey());
					VectorCats currentResult = (VectorCats) v.getValue();
					if (baseResult == null)
						result = currentResult;
					else
						result = baseResult.fold(currentResult);
				} else if (Ledger.class.isAssignableFrom(v.getValue().getClass())) {
					Ledger baseResult = (Ledger) resultMap.get(v.getKey());
					Ledger currentResult = (Ledger) v.getValue();
					if (baseResult == null)
						result = currentResult;
					else
						result = baseResult.fold(currentResult);
				} else {
					log.warning("Ignorning unrecognised query result {}",
							v.getValue().getClass());
					continue;
				}
				resultMap.put(v.getKey(), result);
			}
		}

		for (String key : resultMap.keySet()) {
			// The key is the name of the filter that generated the data
			Foldable<?> value = resultMap.get(key);
			String jobId = currentWorkInfo.work.jobProgressId;
			CellSize resolution = currentWorkInfo.work.outputResolution;
			log.info("value:" + value);
			if (DatasetCats.class.isAssignableFrom(value.getClass())) {
				log.info("DatasetCat matched");
			}
			else if (VectorCats.class.isAssignableFrom(value.getClass())) {
				if (!isTaskCatsExist(jobId, key)) {
					Cats cats = ((VectorCats) value).getComponents()[0];
					cats = cats.optimise();
					statisticsDao.saveCats(new TaskCats(jobId,
						key, resolution, cats,
						cats.getBucketingStrategy().isCategorical()));
				}

			} else if (Ledger.class.isAssignableFrom(value.getClass())) {
				JobProgress progress = jobProgressDao.retrieve(jobId);
				TaskLedger taskLedger = new TaskLedger();
				taskLedger.setJob(progress);
				taskLedger.setKey(key);
				taskLedger.setOutputResolution(resolution);
				taskLedger.setLedger((Ledger) value);
				statisticsDao.saveOrReplaceLedger(taskLedger);

			} else {
				log.warning("Ignorning unrecognised query result {}",
						value.getClass());
				continue;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Foldable<?>> DeserializeResult(String jobProgressId, String result) {
		HashMap<String, Foldable<?>> value = null;
		Path fileName = getOutputPath(jobProgressId, result);
		try {
			FileInputStream fileIn = new FileInputStream(fileName.toString());
			ObjectInputStream in = new ObjectInputStream(fileIn);
			value = (HashMap<String, Foldable<?>>) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i) {
			i.printStackTrace();
			return null;
		}catch(ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return null;
		}
		return value;
	}

	public Path getOutputPath(String jobProgressId, String result) {
		Path outputDir = Paths.get(ndgConfigManager.getConfig()
		.getDefaultPickupLocation() + "/" + jobProgressId + "/temp/" + result);
		return outputDir;
	}
}
