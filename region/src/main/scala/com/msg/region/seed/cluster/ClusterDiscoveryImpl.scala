package com.msg.region.seed.cluster

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.cluster.Cluster
import akka.http.ClientConnectionSettings
import com.msg.region.seed.etcd.EtcdClient

class ClusterDiscoveryImpl(extendedSystem: ExtendedActorSystem) extends Extension {

    private implicit val system = extendedSystem

    val discoverySettings = ClusterDiscoverySettings.load(system.settings.config)

    val httpClientSettings = ClientConnectionSettings(system).copy(
        connectingTimeout = discoverySettings.etcdConnectionTimeout,
        idleTimeout = discoverySettings.etcdRequestTimeout)

    private val etcd = EtcdClient(discoverySettings.etcdHost, discoverySettings.etcdPort, Some(httpClientSettings))

    private val cluster = Cluster(system)
    
    private val discovery = system.actorOf(ClusterDiscoveryActor.props(etcd, cluster, discoverySettings))
    
    def start(): Unit = {
        discovery ! ClusterDiscoveryActor.Start
    }
    
    start

}