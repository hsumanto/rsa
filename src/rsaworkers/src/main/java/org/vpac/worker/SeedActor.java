package org.vpac.worker;

import akka.actor.UntypedActor;

import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.actor.ActorSystem;

public class SeedActor extends UntypedActor {

    ActorSystem system = getContext().system();
    Cluster cluster = Cluster.get(system);

    public SeedActor() {
        System.out.println("SeedActor constructor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("message:" + message);
    }
}
