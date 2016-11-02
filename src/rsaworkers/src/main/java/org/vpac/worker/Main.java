package org.vpac.worker;

import akka.actor.*;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import java.io.IOException;
import java.lang.Runnable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.util.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class Main {
	private static String systemName = "Workers";
	private static FiniteDuration workTimeout = Duration.create(60, "seconds");
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

	public void registerStopSystem(ActorSystem system) {
        Cluster.get(system).registerOnMemberRemoved(new Runnable() {
          @Override
          public void run() {
            // exit JVM when ActorSystem has been terminated
            final Runnable exit = new Runnable() {
              @Override public void run() {
                System.exit(0);
              }
            };
            system.registerOnTermination(exit);

            // shut down ActorSystem
            system.terminate();

            // In case ActorSystem shutdown takes longer than 10 seconds,
            // exit the JVM forcefully anyway.
            // We must spawn a separate thread to not block current thread,
            // since that would have blocked the shutdown of the ActorSystem.
            new Thread() {
              @Override public void run(){
                try {
                  Await.ready(system.whenTerminated(), Duration.create(10, TimeUnit.SECONDS));
                } catch (Exception e) {
                  System.exit(-1);
                }

              }
            }.start();
          }
        });
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		Main main = new Main();
		String role = args[0];
		System.out.println("role " + role);
		if (role.equals("master")) {
			System.out.println("MASTER Started!!!");
			main.initBeans();
			main.startMaster();
		} else if (role.equals("worker")) {
			System.out.println("WORKER Started!!!");
			main.initBeans();
			main.startWorker();
		} else if (role.equals("seed")) {	
			String port = args[1];
			System.out.println("SEED Started!!!");
			main.startSeed(port);
		} else {
			System.out.println("Proper role is needed");
			System.exit(-1);
		}
	}

	public void startSeed(String port) {
		Config conf = ConfigFactory.load("seed");
		Config portConfig = ConfigFactory.parseString("remote.netty.tcp.port=" + port);
		conf = conf.withFallback(portConfig);
		conf = AkkaUtil.patchConfig(conf);
		Config config = ConfigFactory.load(conf);
		ActorSystem system = ActorSystem.create(systemName, config);
		system.actorOf(Props.create(SeedActor.class), "seed");
	}

	public void startMaster() {
		ActorSystem system = createSystem("master");
    	system.actorOf(
        	ClusterSingletonManager.props(
            	Master.props(workTimeout),
            	PoisonPill.getInstance(),
            	ClusterSingletonManagerSettings.create(system).withRole("master")
        	),
        	"master");
		system.actorOf(Props.create(DatabaseActor.class), "database");
	}

	public void startWorker() {
		ActorSystem system = createSystem("worker");
	    ActorRef clusterClient = system.actorOf(
	        ClusterClient.props(ClusterClientSettings.create(system)),
	        "clusterClient");
	    system.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");
	}

	public ActorSystem createSystem(String role) {
		Config conf = ConfigFactory.load(role);
		conf = AkkaUtil.patchConfig(conf);
		ActorSystem system = ActorSystem.create(systemName, conf);
		return system;
	}
}
