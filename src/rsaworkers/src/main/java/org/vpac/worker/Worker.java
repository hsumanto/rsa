package org.vpac.worker;

import akka.actor.*;
import akka.cluster.client.ClusterClient.SendToAll;
import akka.cluster.client.ClusterClient;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;

import java.io.IOException;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

import org.vpac.worker.Job.Work;
import org.vpac.worker.Job.WorkComplete;
import org.vpac.worker.master.Ack;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import static akka.actor.SupervisorStrategy.Directive;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static org.vpac.worker.MasterWorkerProtocol.*;

public class Worker extends UntypedActor {

	public static Props props(ActorRef clusterClient, Props workExecutorProps,
			FiniteDuration registerInterval) {
		return Props.create(Worker.class, clusterClient, workExecutorProps,
				registerInterval);
	}

	public static Props props(ActorRef clusterClient, Props workExecutorProps) {
		return props(clusterClient, workExecutorProps,
				Duration.create(10, "seconds"));
	}

	private ActorRef clusterClient;
	private final Props workExecutorProps;
	private final FiniteDuration registerInterval;
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private final String workerId = UUID.randomUUID().toString();
	private final ActorRef workExecutor;
	private final Cancellable registerTask;
	private String currentWorkId = null;
	Cluster cluster = Cluster.get(getContext().system());

	public Worker(ActorRef clusterClient, Props workExecutorProps,
			FiniteDuration registerInterval) {
		this.clusterClient = clusterClient;
		// for (ActorSelection act: clusterClient.contacts) {
		// 	System.out.println("contact:" + act);
		// }
		this.workExecutorProps = workExecutorProps;
		this.registerInterval = registerInterval;
		this.workExecutor = getContext().watch(
				getContext().actorOf(workExecutorProps, "exec"));
		this.registerTask = getContext()
				.system()
				.scheduler()
				.schedule(
						Duration.Zero(),
						registerInterval,
						clusterClient,
						new SendToAll("/user/master/singleton",
								new RegisterWorker(workerId)),
						getContext().dispatcher(), getSelf());
	}

	private String workId() {
		if (currentWorkId != null)
			return currentWorkId;
		else
			throw new IllegalStateException("Not working");
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return new OneForOneStrategy(-1, Duration.Inf(),
				new Function<Throwable, Directive>() {
					@Override
					public Directive apply(Throwable t) {
						if (t instanceof ActorInitializationException)
							return stop();
						else if (t instanceof DeathPactException)
							return stop();
						else if (t instanceof Exception) {
							if (currentWorkId != null)
								sendToMaster(new WorkFailed(workerId, workId()));
							getContext().become(idle);
							return restart();
						} else {
							return escalate();
						}
					}
				});
	}

	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
		    MemberEvent.class, UnreachableMember.class);
	}

	@Override
	public void postStop() {
		registerTask.cancel();
	}

	public void onReceive(Object message) {
		unhandled(message);
	}

	private final Behavior idle = new Behavior() {
		public void apply(Object message) {
			if (message instanceof MasterWorkerProtocol.WorkIsReady)
				sendToMaster(new MasterWorkerProtocol.WorkerRequestsWork(
						workerId));
			else if (message instanceof Work) {
				Work work = (Work) message;
				log.debug("Got work: {}", work);
				System.out.println("Got work: " + work);
				currentWorkId = work.workId;
				workExecutor.tell(work, getSelf());
				getContext().become(working);
			} else
				unhandled(message);
		}
	};

	private final Behavior working = new Behavior() {
		public void apply(Object message) {
			if (message instanceof WorkComplete) {
				Object result = ((WorkComplete) message).result;
				log.debug("Work is complete. Result {}.", result);
				sendToMaster(new WorkIsDone(workerId, workId(), result));
				getContext().setReceiveTimeout(Duration.create(5, "seconds"));
				getContext().become(waitForWorkIsDoneAck(result));
			} else if (message instanceof ProgressCheckPoint) {
				sendToMaster((ProgressCheckPoint) message);
			} else if (message instanceof Work) {
				log.info("Yikes. Master told me to do work, while I'm working.");
			} else if (message instanceof Job.Error) {
				Job.Error err = (Job.Error) message;
				// Add workerId to allow master to set status of this worker.
				err = new Job.Error(err.work, err.exception, workerId);
				sendToMaster(err);
				sendToMaster(new WorkerRequestsWork(workerId));
				getContext().setReceiveTimeout(Duration.Undefined());
				getContext().become(idle);
			} else {
				unhandled(message);
			}
		}
	};

	private Behavior waitForWorkIsDoneAck(final Object result) {
		return new Behavior() {
			public void apply(Object message) {
				if (message instanceof Ack
						&& ((Ack) message).getWorkId().equals(workId())) {
					sendToMaster(new WorkerRequestsWork(workerId));
					getContext().setReceiveTimeout(Duration.Undefined());
					getContext().become(idle);
				} else if (message instanceof ReceiveTimeout) {
					log.info("No ack from master, retrying (" + workerId
							+ " -> " + workId() + ")");
					sendToMaster(new WorkIsDone(workerId, workId(), result));
				} else {
					unhandled(message);
				}
			}
		};
	}

	{
		getContext().become(idle);
	}

	@Override
	public void unhandled(Object message) {
		if (message instanceof Terminated
				&& ((Terminated) message).getActor().equals(workExecutor)) {
			getContext().stop(getSelf());
		} else if (message instanceof StopWorking) {
			log.info("StopWorking message got");
			getContext().stop(getSelf());
		} else if (message instanceof WorkIsReady) {
			// do nothing
		} else if (message instanceof UnreachableMember) {
			// UnreachableMember mUnreachable = (UnreachableMember) message;
			// log.info("Member detected as unreachable: {}", mUnreachable.member());
		} else if (message instanceof MemberRemoved) {
			MemberRemoved mRemoved = (MemberRemoved) message;
			cluster.leave(mRemoved.member().address());
			log.info("Member is Removed: {}", mRemoved.member());
		} else if (message instanceof MemberUp) {
			// MemberUp mUp = (MemberUp) message;
			// log.info("Member is Up: {}", mUp.member().address().port().get());

			// if (mUp.member().address().port().get().equals("2552")) {
			// 	log.info("Master found: {}", mUp.member());
			// }
		} else {
			super.unhandled(message);
		}
	}

	private void sendToMaster(Object msg) {
		clusterClient
				.tell(new SendToAll("/user/master/singleton", msg), getSelf());
	}
}
