# Concept
The rsaworker is a server system using akka system(http://akka.io). It provides clustered master-worker concept server system. The master - worker concept consists one or more of nodes. So master and worker can be placed in two diffenent nodes and can be placed in one node.
It's based on akka-distributed-worker-java project in typesafe activator template(http://typesafe.com/activator/template/akka-distributed-workers-java)  

# Basic Config
Configuration(resources/master.conf)

include "application"

akka.remote.netty.tcp.hostname = "your ip address to be published"

akka.remote.netty.tcp.port = 2552

// Here is the place need to set master ip and port
// usually master's port = 2552
// if current actorsystem is master, just comment out

//master.hostip = "172.31.66.87"

//master.port = "2552"


1. Master - worker (standalone) : just use commeted out version of master.conf
   rsaworker

2. Master - worker (connection to another master) : uncomment master.hostip and master.port and set it properly(master.conf)
   rsaworker
   
3. Worker only : TBD