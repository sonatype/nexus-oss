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
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

// FIXME: Docs need to be cleaned up once we get something "simple" working

/**
 * The simplified (or not) Nexus booter class, that boots Nexus (the IT test subject) in completely same way as it
 * boots in bundle, but in this same JVM in an isolated classloader, hence, even it will exist in same JVM, REST API is
 * the only possible "contact" with it.
 *
 * Naturally, Java Service Wrapper is not present, but it uses the same Jetty8 class used
 * by bundle boot procedure too. Currently, nexus is started/stopped per test class.
 *
 * <p>
 * There are few trickeries happening here (think ClassLoaders) that will mostly go away once we start producing clean
 * WAR instead of this "bastardized" WAR layout. There are two reasons for this class loader trickery:
 *
 * a) for every Nexus boot, we create new Nexus isolated classloader, but we still keep one "shared" classloader as
 * parent of it, that is never recreated. For reasons, see comments in {@link #tamperJarsForSharedClasspath(File)}
 * method.
 *
 * b) we are emulating what is happening during bundle boot, and since Nexus -- while it is fully fledged Java Web
 * Application -- there is one outstanding exception: the JARs are not in /WEB-INF/lib, but in a folder above /WEB-INF,
 * which is illegal.
 *
 * Hence, we, in same way as booter, add the Nexus constituent JARs to a classpath, and let Jetty8 create
 * WebAppClassloader, that will "delegate" to classes, but filtering out Jetty implementation classes on the way.
 *
 * <p>
 * Again, once we start following conventions, this class would be simplified too! For example, the IT-realm and
 * trickery around it would become not needed at all, since Jetty8 would create WebAppClassloader anyway, lifting
 * everything from /WEB-INF/lib directory.
 *
 * @author cstamas
 */
public class JettyServerNexusBooter
    implements NexusBooter
{
  private static Logger log = LoggerFactory.getLogger(JettyServerNexusBooter.class);

  private static final String IT_REALM_ID = "it-realm";

  private final File bundleBasedir;

  private final ClassWorld world;

  private final ClassRealm bootClassLoader;

  private ClassRealm nexusClassloader;

  private Object jettyServer;

  private final Map<String,String> props = new HashMap<>();

  public JettyServerNexusBooter(final File bundleBasedir, final int port) throws Exception {
    this.bundleBasedir = bundleBasedir.getCanonicalFile();
    log.info("Bundle base directory: {}", bundleBasedir);

    tamperJettyConfiguration(bundleBasedir);

    // FIXME: Load nexus.properties, for now hard-code
    props.put("application-host", "0.0.0.0");
    props.put("application-port", String.valueOf(port));

    props.put("nexus-webapp", new File(bundleBasedir, "nexus").getAbsolutePath());
    props.put("nexus-webapp-context-path", "/nexus");
    props.put("runtime", new File(bundleBasedir, "nexus/WEB-INF").getAbsolutePath());

    props.put("bundleBasedir", bundleBasedir.getAbsolutePath());
    props.put("logback.configurationFile", new File(bundleBasedir, "conf/logback.xml").getAbsolutePath());

    // guice finalizer
    props.put("guice.executor.class", "NONE");

    // Making MI integration in Nexus behave in-sync
    props.put("org.sonatype.nexus.events.IndexerManagerEventInspector.async", Boolean.FALSE.toString());

    // Disable autorouting initialization prevented
    props.put(ConfigImpl.FEATURE_ACTIVE_KEY, Boolean.FALSE.toString());

    // Export everything to system properties
    System.getProperties().putAll(props);

    world = new ClassWorld();
    bootClassLoader = createBootClassLoader();
  }

  private ClassRealm createBootClassLoader() throws Exception {
    File dir = new File(bundleBasedir, "lib");
    log.info("Boot lib directory: {}", dir);

    File[] jars = dir.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".jar");
      }
    });

    List<URL> urls = new ArrayList<>();
    for (File jar : jars) {
      urls.add(jar.toURI().toURL());
    }

    ClassRealm realm = world.newRealm("it-shared", null);

    log.info("Boot ClassPath:");
    for (URL url : urls) {
      log.info("  {}", url);
      realm.addURL(url);
    }

    return realm;
  }

  public void startNexus(final String testId) throws Exception {
    checkState(jettyServer == null, "Nexus already started");

    nexusClassloader = buildNexusClassLoader(testId);
    final ClassLoader original = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(bootClassLoader);

      // we have to get it from shared, as it is present here too, but by mistake it seems
      final Class<?> jettyServer = bootClassLoader.loadClass("org.sonatype.nexus.bootstrap.jetty.JettyServer");

      log.info("Starting Nexus[{}]", testId);

      Constructor factory = jettyServer.getConstructor(ClassLoader.class, Map.class, String[].class);
      this.jettyServer = factory.newInstance(
          nexusClassloader,
          props,
          new String[] { new File(bundleBasedir, "conf/jetty.xml").getAbsolutePath() }
      );
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }

    jettyServer.getClass().getMethod("start").invoke(jettyServer);
  }

  public void stopNexus() throws Exception {
    try {
      log.info("Stopping Nexus");

      if (jettyServer != null) {
        jettyServer.getClass().getMethod("stop").invoke(jettyServer);
      }
    }
    catch (InvocationTargetException e) {
      if (e.getCause() instanceof IllegalStateException) {
        // swallow it, it is Jetty8 that throws this when we stop but did not start...
      }
      else {
        throw (Exception) e.getCause();
      }
    }
    finally {
      clean();
    }
  }

  private ClassRealm buildNexusClassLoader(final String testId) throws Exception {
    return world.newRealm(IT_REALM_ID + "-" + testId, bootClassLoader);
  }

  private File requireFile(final File file) {
    checkState(file.exists(), "Missing required file: %s", file.getAbsolutePath());
    return file;
  }

  private void tamperJettyConfiguration(final File basedir) throws IOException {
    // Disable the shutdown hook, since it disturbs the embedded work
    // In Jetty8, any invocation of server.stopAtShutdown(boolean) will create a thread in a class static member.
    // Hence, we simply want to make sure, that there is NO invocation happening of that method.
    final File file = requireFile(new File(basedir, "conf/jetty.xml"));
    String xml = FileUtils.readFileToString(file, "UTF-8");

    // completely removing the server.stopAtShutdown() method invocation, to try to prevent thread creation at all
    xml = xml.replace(
        "<Set name=\"stopAtShutdown\">true</Set>",
        "<!-- NexusBooter: Set name=\"stopAtShutdown\">true</Set-->"
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

  private void clean() {
    if (nexusClassloader != null) {
      try {
        world.disposeRealm(nexusClassloader.getId());
      }
      catch (NoSuchRealmException e) {
        log.warn("Unexpected; ignoring", e);
      }
    }

    // drop references
    this.jettyServer = null;
    this.nexusClassloader = null;

    // give some relief for other (like JVM internal) threads
    Thread.yield();

    // force GC, may help
    System.gc();
  }
}
