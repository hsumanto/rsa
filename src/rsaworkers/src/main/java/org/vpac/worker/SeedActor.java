package org.vpac.worker;

import akka.actor.UntypedActor;

import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;

public class SeedActor extends UntypedActor {

    Cluster cluster = Cluster.get(getContext().system());

    public SeedActor() {
        System.out.println("SeedActor constructor");
    }

    @Override
    public void onReceive(Object message) throws Exception {
    }
}
