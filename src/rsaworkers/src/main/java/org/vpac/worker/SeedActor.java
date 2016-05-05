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
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
            MemberEvent.class, MemberUp.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof UnreachableMember) {
            UnreachableMember unreachable = (UnreachableMember) message;
            cluster.leave(unreachable.member().address());
            System.out.println("Member is UnreachableMember: " + unreachable.member());
        }
    }
}
