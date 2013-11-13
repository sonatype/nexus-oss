/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.test.booter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;

import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.io.filefilter.FileFilterUtils.filter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

/**
 * Embedded Nexus server booter.
 *
 * @since 2.8
 */
public class EmbeddedNexusBooter
    implements NexusBooter
{
  private static Logger log = LoggerFactory.getLogger(EmbeddedNexusBooter.class);

  private static final String IT_REALM_ID = "it-realm";

  private final File installDir;

  private final int port;

  private final Map<String,String> props;

  private final ClassWorld world;

  private final ClassRealm bootRealm;

  private final Class<?> jettyServerClass;

  private final Constructor jettyServerFactory;

  private ClassRealm testRealm;

  private Object jettyServer;

  public EmbeddedNexusBooter(final File installDir, final int port) throws Exception {
    this.installDir = checkNotNull(installDir).getCanonicalFile();
    log.info("Install directory: {}", installDir);

    checkArgument(port > 1024);
    this.port = port;
    log.info("Port: {}", port);

    props = loadProperties();
    props.put("application-port", String.valueOf(port));

    log.info("Properties:");
    for (Entry<String,String> entry : props.entrySet()) {
      log.info("  {}='{}'", entry.getKey(), entry.getValue());
    }

    // Export everything to system properties
    System.getProperties().putAll(props);

    tamperJettyConfiguration();

    world = new ClassWorld();
    bootRealm = createBootRealm();

    jettyServerClass = bootRealm.loadClass("org.sonatype.nexus.bootstrap.jetty.JettyServer");
    log.info("Jetty server class: {}", jettyServerClass);

    jettyServerFactory = jettyServerClass.getConstructor(ClassLoader.class, Map.class, String[].class);
    log.info("Jetty server factory: {}", jettyServerFactory);
  }

  private Map<String,String> loadProperties() {
    Map<String,String> p = new HashMap<>();

    // FIXME: Load nexus.properties, for now hard-code
    p.put("application-host", "0.0.0.0");
    p.put("nexus-webapp", new File(installDir, "nexus").getAbsolutePath());
    p.put("nexus-webapp-context-path", "/nexus");
    p.put("runtime", new File(installDir, "nexus/WEB-INF").getAbsolutePath());

    p.put("bundleBasedir", installDir.getAbsolutePath());
    p.put("logback.configurationFile", new File(installDir, "conf/logback.xml").getAbsolutePath());

    // guice finalizer
    p.put("guice.executor.class", "NONE");

    // Making MI integration in Nexus behave in-sync
    p.put("org.sonatype.nexus.events.IndexerManagerEventInspector.async", Boolean.FALSE.toString());

    // Disable autorouting initialization prevented
    p.put(ConfigImpl.FEATURE_ACTIVE_KEY, Boolean.FALSE.toString());

    return p;
  }

  private void tamperJettyConfiguration() throws IOException {
    final File file = new File(installDir, "conf/jetty.xml");
    String xml = FileUtils.readFileToString(file, "UTF-8");

    // Disable the shutdown hook, since it disturbs the embedded work
    // In Jetty8, any invocation of server.stopAtShutdown(boolean) will create a thread in a class static member.
    // Hence, we simply want to make sure, that there is NO invocation happening of that method.
    // FIXME: These can be avoided by using a <Property> configuration with default value in jetty.xml
    xml = xml.replace(
        "<Set name=\"stopAtShutdown\">true</Set>",
        "<!-- Set name=\"stopAtShutdown\">true</Set -->"
    );

    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357318#c62
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      xml = xml.replace(
          "org.sonatype.nexus.bootstrap.jetty.InstrumentedSelectChannelConnector",
          "org.eclipse.jetty.server.nio.BlockingChannelConnector"
      );
    }

    FileUtils.writeStringToFile(file, xml, "UTF-8");
  }

  private ClassRealm createBootRealm() throws Exception {
    List<URL> classpath = new ArrayList<>() ;

    File confDir = new File(installDir, "conf");
    log.info("Boot conf dir: {}", confDir);
    classpath.add(confDir.toURI().toURL());

    File libDir = new File(installDir, "lib");
    log.info("Boot lib dir: {}", libDir);
    File[] jars = filter(suffixFileFilter(".jar"), libDir.listFiles());
    for (File jar : jars) {
      classpath.add(jar.toURI().toURL());
    }

    ClassRealm realm = world.newRealm("it-boot", null);
    log.info("Boot classpath:");
    for (URL url : classpath) {
      log.info("  {}", url);
      realm.addURL(url);
    }

    return realm;
  }

  @Override
  public void startNexus(final String testId) throws Exception {
    checkState(jettyServer == null, "Nexus already started");

    testRealm = world.newRealm(IT_REALM_ID + "-" + testId, bootRealm);

    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(bootRealm);

    try {
      log.info("Starting Nexus[{}]", testId);

      jettyServer = jettyServerFactory.newInstance(
          testRealm,
          props,
          new String[] { new File(installDir, "conf/jetty.xml").getAbsolutePath() }
      );
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }

    jettyServerClass.getMethod("start").invoke(jettyServer);
  }

  @Override
  public void stopNexus() throws Exception {
    try {
      log.info("Stopping Nexus");

      if (jettyServer != null) {
        jettyServerClass.getMethod("stop").invoke(jettyServer);
      }
      jettyServer = null;
    }
    catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IllegalStateException) {
        log.debug("Ignoring", cause);
      }
      else {
        log.error("Stop failed", cause);
        throw Throwables.propagate(cause);
      }
    }
    finally {
      if (testRealm != null) {
        try {
          world.disposeRealm(testRealm.getId());
        }
        catch (NoSuchRealmException e) {
          log.warn("Unexpected; ignoring", e);
        }
      }
      testRealm = null;

      Thread.yield();
      System.gc();
    }
  }
}
