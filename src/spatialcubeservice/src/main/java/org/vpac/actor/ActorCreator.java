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
import org.vpac.worker.AkkaUtil;

public class ActorCreator {
	private static ActorCreator instance = null;
	private static ActorSystem system = null;
	private static ActorRef frontend = null;

	private ActorCreator() {
		Config conf = ConfigFactory.load();
		conf = AkkaUtil.patchConfig(conf);
		ActorCreator.system = ActorSystem.create("Workers", conf);
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
