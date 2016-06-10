package org.vpac.worker;

import akka.actor.*;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import java.io.IOException;
import java.lang.Runnable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.util.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class AkkaUtil {

    public static Config patchConfig(Config conf) {
        List<String> seedUris;
        List<String> patchedUris;
        ConfigValue patchedValue;

        seedUris = conf.getStringList("akka.cluster.seed-nodes");
        patchedUris = convertHostnamesToIps(seedUris);
        patchedValue = ConfigValueFactory.fromIterable(patchedUris);
        conf = conf.withValue("akka.cluster.seed-nodes", patchedValue);

        seedUris = conf.getStringList("akka.cluster.client.initial-contacts");
        patchedUris = convertHostnamesToIps(seedUris);
        patchedValue = ConfigValueFactory.fromIterable(patchedUris);
        conf = conf.withValue("akka.cluster.client.initial-contacts", patchedValue);

        return conf;
    }

    private static List<String> convertHostnamesToIps(List<String> uris) {
        List<String> patchedUris = new ArrayList<>();
        for (String uriStr : uris) {
            URI uri;
            String host;
            try {
                uri = new URI(uriStr);
                host = InetAddress.getByName(uri.getHost()).getHostAddress();
                uri = new URI(uri.getScheme(), uri.getUserInfo(), host,
                    uri.getPort(), uri.getPath(), uri.getQuery(),
                    uri.getFragment());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            patchedUris.add(uri.toString());
        }
        return patchedUris;
    }
}
