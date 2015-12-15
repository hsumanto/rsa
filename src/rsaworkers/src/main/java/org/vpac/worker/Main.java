package org.vpac.worker;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterClient;
import akka.contrib.pattern.ClusterSingletonManager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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

	public void startService() throws InterruptedException, IOException {

		if (isMaster()) {
			startMaster();
			System.out.println("Master started");
		} else {
			System.out.println("Worker started");
			startWorker();
		}
	}
	
	public static Boolean isMaster() {
		Boolean isMaster;
		if (System.getenv("RSA_IS_MASTER") == null)
			isMaster = false;
		else
			isMaster = Boolean.parseBoolean(System.getenv("RSA_IS_MASTER"));
		return isMaster;
	}
	
	public static String searchCluster() throws IOException{
	   int timeout=1000;
	   InetAddress localhost = InetAddress.getLocalHost();
	   String returnAddress = null;
	   byte[] ip = localhost.getAddress();
	   for (int i = 1; i <= 254; i++) {
			ip[3] = (byte)i;
			InetAddress address = InetAddress.getByAddress(ip);
			if (address.isReachable(timeout)) {
				Socket socket = new Socket();
				System.out.println("localhost:" + localhost.getHostAddress());
				System.out.println("address:" + address.getHostAddress());
				if (localhost.getHostAddress().equals(address.getHostAddress())) {
					break;
				} else {
					try {
				        socket.connect(new InetSocketAddress(address, 2552), timeout);
				        System.out.println("Success for connection");
				        returnAddress = address.getHostAddress();
				        break;
					} catch (Exception e) {
						System.out.println("Connection fail for " + address.getHostAddress());
					} finally {
				        socket.close();					
					}				
				}
			}
			System.out.println("i:" + i);
	   }
	   return returnAddress;
	}	

	public static void main(String[] args) throws InterruptedException, IOException {
		Main main = new Main();
		main.initBeans();
		main.startService();
	}

	private static String systemName = "Workers";
	private static FiniteDuration workTimeout = Duration.create(100, "minutes");

	public static void startMaster() throws IOException {
		ActorSystem system = createSystem();
		Address joinAddress = getAddress(system);
		
		Cluster.get(system).join(joinAddress);

		ActorRef master = system.actorOf(ClusterSingletonManager.defaultProps(
				Master.props(workTimeout), "active", PoisonPill.getInstance(),
				"backend"), "master");
		System.out.println("master Path:" + master.toString());
		if(isMaster()) {
			ActorRef database = system.actorOf(Props.create(DatabaseActor.class), "database");
			System.out.println("database Path:" + database.toString());
		}
	}

	public static void startWorker() throws IOException {
		ActorSystem system = createSystem();
		Address joinAddress = getAddress(system);
		
		Cluster.get(system).join(joinAddress);

		Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();
		initialContacts.add(system.actorSelection(joinAddress
				+ "/user/receptionist"));
		ActorRef clusterClient = system.actorOf(
				ClusterClient.defaultProps(initialContacts), "clusterClient");
		system.actorOf(
				Worker.props(clusterClient, Props.create(WorkExecutor.class)),
				"worker");
	}
	
	public static ActorSystem createSystem() {
		Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
				.withFallback(ConfigFactory.load());
		conf.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port = 0"));

		try {
			conf.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		
		
		ActorSystem system = ActorSystem.create(systemName, conf);
		return system;
	}
	
	public static Address getAddress(ActorSystem system) throws IOException {
		
		Address joinAddress = null;

		String existingCluster = searchCluster();
		if (existingCluster == null) {
			joinAddress = Cluster.get(system).selfAddress();
		} else {
			joinAddress = new Address("akka.tcp", "Workers", existingCluster, 2552);
		}
		return joinAddress;
	}
}