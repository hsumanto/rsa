package org.vpac.actor;

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
		Config config = ConfigFactory.parseString("akka.actor.provider = akka.cluster.ClusterActorRefProvider").
				withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port = 0")).
				withFallback(ConfigFactory.parseString("akka.extensions = [akka.contrib.pattern.ClusterReceptionistExtension]"));
		ActorCreator.system = ActorSystem.create("Workers", config);
		Config c = ConfigFactory.load("master");
		String hostip = c.getString("master.hostip").toString();
		int port = Integer.parseInt(c.getString("master.port").toString());

		Address address = new Address("akka.tcp", "Workers", hostip, port);
		Cluster.get(system).join(address);
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
}
