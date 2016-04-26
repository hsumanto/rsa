package org.vpac.worker;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.Await;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.InterfaceAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.lang.Runnable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;
import org.apache.commons.net.util.*;

public class Main {
	private static String systemName = "Workers";
	private static FiniteDuration workTimeout = Duration.create(10, "seconds");
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

	public void initBeans() {
		ApplicationContext appContext = AppContextSingleton.INSTANCE.appContext;

		DatasetProvider dataProvider = (DatasetProvider) appContext
				.getBean("rsaDatasetProvider");
		DatasetProvider fileDatasetProvider = (DatasetProvider) appContext
				.getBean("fileDatasetProvider");
		ProviderRegistry.getInstance().clearProivders();
		ProviderRegistry.getInstance().addProvider(dataProvider);
		ProviderRegistry.getInstance().addProvider(fileDatasetProvider);
	}

	public void startService() {
		Address masterAddress = startMaster();
		startWorker(masterAddress);
	}

	public void startSeed() {
		Config conf = ConfigFactory.load("seed");
		ActorSystem system = ActorSystem.create(systemName, conf);
		system.actorOf(Props.create(SeedActor.class), "seed");
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		Main main = new Main();
		if (args.length == 0) {
			main.initBeans();
			main.startService();
		} else {
			System.out.println("SEED Started!!!");
			main.startSeed();
		}
	}

	public static Address startMaster() {
		ActorSystem system = createSystem("master");
    	system.actorOf(
        	ClusterSingletonManager.props(
            	Master.props(workTimeout),
            	PoisonPill.getInstance(),
            	ClusterSingletonManagerSettings.create(system).withRole("master")
        	),
        	"master");

		system.actorOf(Props.create(DatabaseActor.class), "database");
		return Cluster.get(system).selfAddress();
	}

	public static void startWorker(Address masterAddress) {
		ActorSystem system = createSystem("worker");
		System.out.println("Worker Started!!!" + masterAddress);

	    ActorRef clusterClient = system.actorOf(
	        ClusterClient.props(ClusterClientSettings.create(system)),
	        "clusterClient");

	    system.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");
	}
	
	public static ActorSystem createSystem(String role) {
		Config conf = ConfigFactory.load(role);
		ActorSystem system = ActorSystem.create(systemName, conf);
		return system;
	}
}