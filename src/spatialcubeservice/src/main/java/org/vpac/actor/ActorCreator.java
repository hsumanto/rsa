package org.vpac.actor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.net.util.*;

public class ActorCreator {
	private static ActorCreator instance = null;
	private static ActorSystem system = null;
	private static ActorRef frontend = null;
	static final ExecutorService es = Executors.newFixedThreadPool(20);
	static final int timeout = 200;
	static final int akkaPort = 2552;


	private ActorCreator() {
		Config conf = ConfigFactory.parseString("akka.cluster.roles=[frontend]")
				.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2554"))
				.withFallback(ConfigFactory.load());		
		ActorCreator.system = ActorSystem.create("Workers", conf);

		Address joinAddress = null;
		try {
			joinAddress = getAddress(system);
		} catch (IOException ioe) {
			System.out.println("Cannot find join address. Please contact system administator.");
			return;
		}

		Cluster.get(system).join(joinAddress);
		System.out.println("Web started.\n Connected on Master-" + joinAddress);
		ActorCreator.frontend = system.actorOf(Props.create(Frontend.class), "frontend");
	}
	
	public static ActorCreator createActorCreator() {
		if(instance == null) 
			instance = new ActorCreator();
		return instance;
	}
	
	public static ActorSystem getActorSystem() {
		if(instance == null) 
			createActorCreator();
		return ActorCreator.system;
	}

	public static ActorRef getFrontend() {
		if(instance == null) 
			createActorCreator();
		return ActorCreator.frontend;
	}

	public static String searchCluster() throws IOException {
		InetAddress localhost = Inet4Address.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
		String returnAddress = null;
		String subnet = null;

		for (InterfaceAddress ip : networkInterface.getInterfaceAddresses()) {
			if(ip.getAddress().toString().contains(localhost.getHostAddress())) {
				subnet = ip.toString().substring(1).split(" ")[0];
			}
		}

//		subnet = subnet.replace("/16", "/24");
//		System.out.println("subnet:" + subnet);
		SubnetUtils utils = new SubnetUtils(subnet);
		String[] allIps = utils.getInfo().getAllAddresses();
		List<Future<String>> futures = new ArrayList<>();

		for (String address : allIps) {
			futures.add(portIsOpen(es, address, akkaPort, timeout));
		}

		try {
			for (Future<String> f : futures) {
			    if (f.get() != null) {
					System.out.println("SOME PORT OPEN");
			    }
			  }

			Thread.sleep(10000);
		} catch (Exception e) {
//			log.info("sleeping error!");
		}
	    return returnAddress;
	}	

	public static Address getAddress(ActorSystem system) throws IOException {
		
		Address joinAddress = null;

		String existingCluster = null;
		while (existingCluster == null) {
			existingCluster = searchCluster();
			if (existingCluster != null) {
				joinAddress = new Address("akka.tcp", "Workers", existingCluster, akkaPort);
				break;
			}
		}
		return joinAddress;
	}


	public static Future<String> portIsOpen(final ExecutorService es, String ip, int port, final int timeout) {
	  return es.submit(new Callable<String>() {
	      @Override public String call() {
	        try {
	          Socket socket = new Socket();
	          socket.connect(new InetSocketAddress(ip, port), timeout);
	          socket.close();
	          return ip;
	        } catch (Exception ex) {
	          return null;
	        }
	      }
	   });
	}
}
