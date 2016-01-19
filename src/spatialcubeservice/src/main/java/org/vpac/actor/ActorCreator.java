package org.vpac.actor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterClient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.nmap4j.Nmap4j;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.nmap4j.data.NMapRun;
import org.nmap4j.data.host.ports.Port;
import org.nmap4j.data.nmaprun.Host;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorCreator {
	private static ActorCreator instance = null;
	private static ActorSystem system = null;
	private static ActorRef frontend = null;
	
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
