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

import org.nmap4j.*;
import org.nmap4j.data.*;
import org.nmap4j.core.nmap.*;
import org.nmap4j.data.nmaprun.Host;
import org.nmap4j.data.host.ports.Port;

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
		Address masterAddress = startMaster();
		startWorker(masterAddress);
	}
		
	public static String searchCluster() throws IOException {
		InetAddress localhost = Inet4Address.getLocalHost();
		String returnAddress = null;
		Nmap4j nmap = new Nmap4j("/usr");
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
		InterfaceAddress address = networkInterface.getInterfaceAddresses().get(1);
		nmap.includeHosts(address.getAddress().toString().replace("/", "") + "/24");
		nmap.excludeHosts(localhost.getHostAddress());
		nmap.addFlags("-p 2552");
		try {
			nmap.execute();
		} catch (NMapInitializationException e) {
			System.out.println("Nmap error:" + e);
		} catch (NMapExecutionException ne) {
			System.out.println("Nmap error:" + ne);			
		}
 		if(!nmap.hasError()) { 
			NMapRun nmapRun = nmap.getResult() ; 
			for (Host host: nmapRun.getHosts()) {
				for (Port port: host.getPorts().getPorts()) {
					if(port.getPortId() == 2552 && port.getState().getState().equals("open")) {
						System.out.println("Found cluster-" + host.getAddresses().get(0) + ":" + port.toString() +" - " + port.getState());
						returnAddress = host.getAddresses().get(0).getAddr();
						break;
					}
				}
				if (returnAddress != null)
					break;
			}
		} else { 
			System.out.println(nmap.getExecutionResults().getErrors()); 
		}
		System.out.println("returnAddress:" + returnAddress);
	    return returnAddress;
	}	

	public static void main(String[] args) throws InterruptedException, IOException {
		Main main = new Main();
		main.initBeans();
		main.startService();
	}

	private static String systemName = "Workers";
	private static FiniteDuration workTimeout = Duration.create(100, "minutes");

	public static Address startMaster() throws IOException {
		ActorSystem system = createSystem("master");
		Address joinAddress = getAddress(system);
		Cluster.get(system).join(joinAddress);
   		//startupSharedJournal(system, (port == 2551),
        //ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"));

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

	public static void startWorker(Address masterAddress) throws IOException {
		ActorSystem system = createSystem("worker");
		//Cluster.get(system).join(masterAddress);
		Set<ActorPath> initialContacts = new HashSet<ActorPath>();
		initialContacts.add(ActorPaths.fromString(masterAddress + "/system/receptionist"));

    	ActorRef clusterClient = system.actorOf(
        	ClusterClient.props(ClusterClientSettings.create(system).withInitialContacts(initialContacts)),
        	"clusterClient");
		ActorRef worker = system.actorOf(
				Worker.props(clusterClient, Props.create(WorkExecutor.class)),
				"worker");
	}
	
	public static ActorSystem createSystem(String role) {
		Config conf = null;
		String localIpAddress = null;

		try {
			localIpAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}		

		if (role.equals("master")) {
			conf = ConfigFactory.parseString("akka.cluster.roles=[master]")
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2552"))
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + localIpAddress))
				.withFallback(ConfigFactory.load());
		} else {
			conf = ConfigFactory.parseString("akka.cluster.roles=[worker]")
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2553"))
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = " + localIpAddress))
				.withFallback(ConfigFactory.load());
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