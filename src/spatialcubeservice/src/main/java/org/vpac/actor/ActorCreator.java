package org.vpac.actor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorCreator {
	private static ActorCreator instance = null;
	private static ActorSystem system = null;
	private static ActorRef frontend = null;
	
	private ActorCreator() {
		Config conf = ConfigFactory.parseString("akka.cluster.roles=[backend]")
				.withFallback(ConfigFactory.load());
		conf.withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port = 0"));
		
		Address joinAddress = null;
		String hostname = conf.getString("akka.master.hostname").toString();
		String hostip = null;
		try {
			hostip = InetAddress.getByName(hostname).getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = Integer.parseInt(conf.getString("akka.master.port").toString());

		System.out.println("Web started.\n Connected on Master-" + hostip + ":" + port);
		if (hostip != null)
			joinAddress = new Address("akka.tcp", "Workers", hostip, port);

		ActorCreator.system = ActorSystem.create("Workers", conf);
		Cluster.get(system).join(joinAddress);
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
}
