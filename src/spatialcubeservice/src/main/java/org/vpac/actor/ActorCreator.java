package org.vpac.actor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import akka.actor.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterClient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorCreator {
	private static ActorCreator instance = null;
	private static ActorSystem system = null;
	private static ActorRef frontend = null;
	
	private ActorCreator() {
		Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
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
		Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();
		initialContacts.add(system.actorSelection(joinAddress
				+ "/user/receptionist"));
		ActorRef clusterClient = system.actorOf(
				ClusterClient.defaultProps(initialContacts), "clusterClient");

		ActorCreator.frontend = system.actorOf(Props.create(Frontend.class), "frontend");
		System.out.println("frontend: " + ActorCreator.frontend.toString());		
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
