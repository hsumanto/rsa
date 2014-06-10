package org.vpac.worker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.AppContext;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.worker.Job.Work;
import org.vpac.worker.MasterWorkerProtocol.RegisterWorker;
import org.vpac.worker.MasterWorkerProtocol.WorkFailed;
import org.vpac.worker.MasterWorkerProtocol.WorkIsDone;
import org.vpac.worker.MasterWorkerProtocol.WorkIsReady;
import org.vpac.worker.MasterWorkerProtocol.WorkerRequestsWork;
import org.vpac.worker.master.Ack;
import org.vpac.worker.master.WorkResult;

import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.contrib.pattern.DistributedPubSubMediator.Put;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Master extends UntypedActor {

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
	
	public static String ResultsTopic = "results";

	public static Props props(FiniteDuration workTimeout) {
		return Props.create(Master.class, workTimeout);
	}

	private final FiniteDuration workTimeout;
	private final ActorRef mediator = DistributedPubSubExtension.get(
			getContext().system()).mediator();
	private final LoggingAdapter log = Logging.getLogger(getContext().system(),
			this);
	private final Cancellable cleanupTask;
	private HashMap<String, WorkerState> workers = new HashMap<String, WorkerState>();
	private Queue<Work> pendingWork = new LinkedList<Work>();
	private Set<String> workIds = new LinkedHashSet<String>();
	private Map<Work, Double> workProgress = new HashMap<Work, Double>();
	private JobProgressDao jobProgressDao;

	public Master(FiniteDuration workTimeout) {
		this.workTimeout = workTimeout;
		mediator.tell(new Put(getSelf()), getSelf());
		this.cleanupTask = getContext()
				.system()
				.scheduler()
				.schedule(workTimeout.div(2), workTimeout.div(2), getSelf(),
						CleanupTick, getContext().dispatcher(), getSelf());
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;
		jobProgressDao = (JobProgressDao) appContext.getBean("jobProgressDao");
	}

	@Override
	public void postStop() {
		cleanupTask.cancel();
	}

	@Override
	public void onReceive(Object message) {
		if (message instanceof RegisterWorker) {
			RegisterWorker msg = (RegisterWorker) message;
			String workerId = msg.workerId;
			if (workers.containsKey(workerId)) {
				workers.put(workerId,
						workers.get(workerId).copyWithRef(getSender()));
			} else {
				log.debug("Worker registered: {}", workerId);
				workers.put(workerId, new WorkerState(getSender(),
						Idle.instance));
				if (!pendingWork.isEmpty())
					getSender().tell(WorkIsReady.getInstance(), getSelf());
			}
		} else if (message instanceof WorkerRequestsWork) {
			WorkerRequestsWork msg = (WorkerRequestsWork) message;
			String workerId = msg.workerId;
			if (!pendingWork.isEmpty()) {
				WorkerState state = workers.get(workerId);
				if (state != null && state.status.isIdle()) {
					Work work = pendingWork.remove();
					log.debug("Giving worker {} some work {}", workerId, "");
					// TODO store in Eventsourced
					getSender().tell(work, getSelf());
					workers.put(workerId, state.copyWithStatus(new Busy(work,
							workTimeout.fromNow())));
				}
			}
		} else if (message instanceof WorkIsDone) {
			WorkIsDone msg = (WorkIsDone) message;
			String workerId = msg.workerId;
			String workId = msg.workId;
			Work currentWork = null;
			for(Work w : workProgress.keySet()) {
				if (w.workId.equals(workId)) {
					currentWork = w;
					workProgress.put(w, 100.0);
					break;
				}
			}
			double progress = 0.0;
			int noOfWork = 0;
			for(Work w : workProgress.keySet()) {
				if (w.jobProgressId.equals(currentWork.jobProgressId)) {
					progress += workProgress.get(w).doubleValue();
				}
				noOfWork++;
			}
			System.out.println("Progress:" + progress);
			System.out.println("noOfWork:" + noOfWork);
			System.out.println("calc:" + progress / (noOfWork * 10000) + "%");
			JobProgress job = jobProgressDao.retrieve(currentWork.jobProgressId);
			job.setCurrentStepProgress(progress / (noOfWork * 10000));
			if(progress / (noOfWork * 100) == 1)
				job.setCompleted();
			jobProgressDao.save(job);
			WorkerState state = workers.get(workerId);

			
			if (state != null && state.status.isBusy()
					&& state.status.getWork().workId.equals(workId)) {
				Work work = state.status.getWork();
				Object result = msg.result;
				log.debug("Work is done: {} => {} by worker {}", work, result,
						workerId);
				System.out.println("Work is done: " + work + " => " + result
						+ " by worker " + workerId);
				// TODO store in Eventsourced
				workers.put(workerId, state.copyWithStatus(Idle.instance));
				mediator.tell(new DistributedPubSubMediator.Publish(
						ResultsTopic, new WorkResult(workId, result)),
						getSelf());
				getSender().tell(new Ack(workId), getSelf());
			} else {
				if (workIds.contains(workId)) {
					// previous Ack was lost, confirm again that this is done
					getSender().tell(new Ack(workId), getSelf());
				}
			}
		} else if (message instanceof WorkFailed) {
			WorkFailed msg = (WorkFailed) message;
			String workerId = msg.workerId;
			String workId = msg.workId;
			WorkerState state = workers.get(workerId);
			if (state != null && state.status.isBusy()
					&& state.status.getWork().workId.equals(workId)) {
				log.info("Work failed: {}", state.status.getWork());
				// TODO store in Eventsourced
				workers.put(workerId, state.copyWithStatus(Idle.instance));
				pendingWork.add(state.status.getWork());
				notifyWorkers();
			}
		} else if (message instanceof Work) {
			Work work = (Work) message;
			// idempotent
			if (workIds.contains(work.workId)) {
				getSender().tell(new Ack(work.workId), getSelf());
			} else {
				log.debug("Accepted work: {}", work);
				// TODO store in Eventsourced
				pendingWork.add(work);
				workIds.add(work.workId);
				workProgress.put(work, 0.0);
				getSender().tell(new Ack(work.workId), getSelf());
				notifyWorkers();
			}
		} else if (message == CleanupTick) {
			Iterator<Map.Entry<String, WorkerState>> iterator = workers
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, WorkerState> entry = iterator.next();
				String workerId = entry.getKey();
				WorkerState state = entry.getValue();
				if (state.status.isBusy()) {
					if (state.status.getDeadLine().isOverdue()) {
						Work work = state.status.getWork();
						log.info("Work timed out: {}", work);
						// TODO store in Eventsourced
						iterator.remove();
						pendingWork.add(work);
						notifyWorkers();
					}
				}
			}
		} else {
			unhandled(message);
		}
	}

	private void notifyWorkers() {
		if (!pendingWork.isEmpty()) {
			// could pick a few random instead of all
			for (WorkerState state : workers.values()) {
				if (state.status.isIdle())
					state.ref.tell(WorkIsReady.getInstance(), getSelf());
			}
		}
	}

	private static abstract class WorkerStatus {
		protected abstract boolean isIdle();

		private boolean isBusy() {
			return !isIdle();
		};

		protected abstract Work getWork();

		protected abstract Deadline getDeadLine();
	}

	private static final class Idle extends WorkerStatus {
		private static final Idle instance = new Idle();

		public static Idle getInstance() {
			return instance;
		}

		@Override
		protected boolean isIdle() {
			return true;
		}

		@Override
		protected Work getWork() {
			throw new IllegalAccessError();
		}

		@Override
		protected Deadline getDeadLine() {
			throw new IllegalAccessError();
		}

		@Override
		public String toString() {
			return "Idle";
		}
	}

	private static final class Busy extends WorkerStatus {
		private final Work work;
		private final Deadline deadline;

		private Busy(Work work, Deadline deadline) {
			this.work = work;
			this.deadline = deadline;
		}

		@Override
		protected boolean isIdle() {
			return false;
		}

		@Override
		protected Work getWork() {
			return work;
		}

		@Override
		protected Deadline getDeadLine() {
			return deadline;
		}

		@Override
		public String toString() {
			return "Busy{" + "work=" + work + ", deadline=" + deadline + '}';
		}
	}

	private static final class WorkerState {
		public final ActorRef ref;
		public final WorkerStatus status;

		private WorkerState(ActorRef ref, WorkerStatus status) {
			this.ref = ref;
			this.status = status;
		}

		private WorkerState copyWithRef(ActorRef ref) {
			return new WorkerState(ref, this.status);
		}

		private WorkerState copyWithStatus(WorkerStatus status) {
			return new WorkerState(this.ref, status);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			WorkerState that = (WorkerState) o;

			if (!ref.equals(that.ref))
				return false;
			if (!status.equals(that.status))
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = ref.hashCode();
			result = 31 * result + status.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "WorkerState{" + "ref=" + ref + ", status=" + status + '}';
		}
	}

	private static final Object CleanupTick = new Object() {
		@Override
		public String toString() {
			return "CleanupTick";
		}
	};

	// TODO cleanup old workers
	// TODO cleanup old workIds

}
