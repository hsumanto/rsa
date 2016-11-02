package org.vpac.worker;

import akka.actor.UntypedActor;

import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SeedActor extends UntypedActor {

    ActorSystem system = getContext().system();
    Cluster cluster = Cluster.get(system);
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public SeedActor() {
        System.out.println("SeedActor constructor");
    }

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
            MemberEvent.class, UnreachableMember.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("message:" + message);
        if (message instanceof UnreachableMember) {
            UnreachableMember mUnreachable = (UnreachableMember) message;
            log.info("Member detected as unreachable: {}", mUnreachable.member());
        } else if (message instanceof MemberRemoved) {
            MemberRemoved mRemoved = (MemberRemoved) message;
            log.info("Member is Removed: {}", mRemoved.member());
        }
    }
}
