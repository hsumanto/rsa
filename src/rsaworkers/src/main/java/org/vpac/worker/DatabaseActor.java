package org.vpac.worker;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.AppContext;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.VectorCats;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.dao.StatisticsDao;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.model.TaskCats;
import org.vpac.worker.MasterDatabaseProtocol.JobUpdate;
import org.vpac.worker.MasterDatabaseProtocol.SaveCats;

import akka.actor.UntypedActor;

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
			System.out.println("Path");
			appContext = new ClassPathXmlApplicationContext(
					new String[] { "spring/config/BeanLocations.xml" });

		}
	}

	private JobProgressDao jobProgressDao;
	private StatisticsDao statisticsDao;

	public DatabaseActor() {
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;
		statisticsDao = (StatisticsDao) appContext.getBean("statisticsDao");
		jobProgressDao = (JobProgressDao) appContext.getBean("jobProgressDao");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof JobUpdate) {
			JobUpdate job = (JobUpdate) message;
			JobProgress progress = jobProgressDao.retrieve(job.jobId);
			progress.setCurrentStepProgress(100 * job.fraction);
			if (job.state == TaskState.FINISHED)
				progress.setCompleted();
			jobProgressDao.save(progress);
		} else if (message instanceof SaveCats) {
			SaveCats saveCats = (SaveCats) message;
			if (!isTaskCatsExist(saveCats.jobId, saveCats.key)) {
				Cats cats = ((VectorCats) saveCats.cats).getComponents()[0];
				cats = cats.optimise();
				statisticsDao.saveCats(new TaskCats(saveCats.jobId,
						saveCats.key, saveCats.outputResolution, cats, cats
								.getBucketingStrategy().isCategorical()));
			}
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

}
