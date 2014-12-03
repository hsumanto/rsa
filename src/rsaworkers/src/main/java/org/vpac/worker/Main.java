package org.vpac.worker;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterClient;
import akka.contrib.pattern.ClusterSingletonManager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;

public class Main {
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

	public void startService() throws InterruptedException {
		Address joinAddress = null;
		Config c = ConfigFactory.load("master");
		Boolean isMaster;
		if (System.getenv("RSA_IS_MASTER") == null)
			isMaster = c.hasPath("master.hostname");
		else
			isMaster = Boolean.parseBoolean(System.getenv("RSA_IS_MASTER"));
		System.out.println("isMaster" + isMaster);

		if (!isMaster) {
			String hostname = c.getString("master.hostname").toString();
			String hostip = null;
			try {
				hostip = InetAddress.getByName(hostname).getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			int port = Integer.parseInt(c.getString("master.port").toString());

			System.out.println("Worker started.\n Connected on Master-" + hostip + ":" + port);
			if (hostip != null)
				joinAddress = new Address("akka.tcp", "Workers", hostip, port);

		} else {
			System.out.println("Master started");
		}

		joinAddress = startBackend(joinAddress);
		Thread.sleep(5000);
		startWorker(joinAddress);
	}

	public static void main(String[] args) throws InterruptedException {
		Main main = new Main();
		main.initBeans();
		main.startService();
	}

	private static String systemName = "Workers";
	private static FiniteDuration workTimeout = Duration.create(100, "minutes");

	public static Address startBackend(Address joinAddress) {
		Boolean isMaster = Boolean.parseBoolean(System.getenv("isMaster"));
		Config conf = null;
		if(isMaster) {
			conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
					.withFallback(ConfigFactory.load("master"));
		} else {
			conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
					.withFallback(ConfigFactory.load("worker"));
		}
		try {
			conf.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		ActorSystem system = ActorSystem.create(systemName, conf);
		Address realJoinAddress = (joinAddress == null) ? Cluster.get(system)
				.selfAddress() : joinAddress;
		System.out.println("Address protocol:" + realJoinAddress.protocol());
		System.out.println("Address:" + realJoinAddress.toString());
		Cluster.get(system).join(realJoinAddress);
		System.out.println("Address:" + realJoinAddress.toString());

		system.actorOf(ClusterSingletonManager.defaultProps(
				Master.props(workTimeout), "active", PoisonPill.getInstance(),
				"backend"), "master");
		if(isMaster) {
			system.actorOf(Props.create(DatabaseActor.class), "database");
		}
		return realJoinAddress;
	}

	public static void startWorker(Address contactAddress) {
		Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
				.withFallback(ConfigFactory.load("worker"));
		ActorSystem system = ActorSystem.create(systemName, conf);
		Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();
		initialContacts.add(system.actorSelection(contactAddress
				+ "/user/receptionist"));
		ActorRef clusterClient = system.actorOf(
				ClusterClient.defaultProps(initialContacts), "clusterClient");
		system.actorOf(
				Worker.props(clusterClient, Props.create(WorkExecutor.class)),
				"worker");
	}
}