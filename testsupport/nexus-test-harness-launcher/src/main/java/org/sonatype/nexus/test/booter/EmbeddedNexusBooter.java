/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
import java.lang.reflect.Field;
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

  private final Map<String,String> overrides;

  private final ClassWorld world;

  private final ClassRealm bootRealm;

  private final Class<?> launcherClass;

  private final Constructor launcherFactory;

  private Object launcher;

  public EmbeddedNexusBooter(final File installDir, final int port) throws Exception {
    this.installDir = checkNotNull(installDir).getCanonicalFile();
    log.info("Install directory: {}", installDir);

    // HACK: This is non-standard setup by the test-enviroinment (AbstractEnvironmentMojo)
    File workDir = new File(installDir, "../sonatype-work").getCanonicalFile();
    log.info("Work directory: {}", workDir);

    checkArgument(port > 1024);
    log.info("Port: {}", port);

    overrides = new HashMap<>();
    overrides.put("application-port", String.valueOf(port));
    overrides.put("nexus-base", installDir.getPath());
    overrides.put("nexus-work", workDir.getPath());

    // force bootstrap logback configuration
    overrides.put("logback.configurationFile", new File(installDir, "etc/logback.xml").getPath());

    // Make sure H2 uses TCCL for Java deserialization
    overrides.put("h2.useThreadContextClassLoader", "true");

    // Making MI integration in Nexus behave in-sync
    overrides.put("org.sonatype.nexus.events.IndexerManagerEventInspector.async", Boolean.FALSE.toString());

    // Disable autorouting initialization prevented
    overrides.put(ConfigImpl.FEATURE_ACTIVE_KEY, Boolean.FALSE.toString());

    // Karaf configuration
    String base = installDir.getCanonicalPath();
    overrides.put("karaf.base", base);
    overrides.put("karaf.home", base);
    overrides.put("karaf.data", base + File.separatorChar + "data");
    overrides.put("karaf.etc", base + File.separatorChar + "etc");
    overrides.put("karaf.instances", base + File.separatorChar + "instances");
    overrides.put("karaf.startLocalConsole", "false");
    overrides.put("karaf.startRemoteShell", "false");
    overrides.put("karaf.clean.cache", "true");

    // move tmp under sonatype-work to avoid contamination between tests
    overrides.put("java.io.tmpdir", new File(workDir, "tmp").getPath());

    log.info("Overrides:");
    for (Entry<String,String> entry : overrides.entrySet()) {
      log.info("  {}='{}'", entry.getKey(), entry.getValue());
    }

    tamperKarafConfiguration();
    tamperJettyConfiguration();

    world = new ClassWorld();
    bootRealm = createBootRealm();

    launcherClass = bootRealm.loadClass("org.apache.karaf.main.Main");
    log.info("Launcher class: {}", launcherClass);

    launcherFactory = launcherClass.getConstructor(String[].class);
    log.info("Launcher factory: {}", launcherFactory);
  }

  private void tamperKarafConfiguration() throws IOException {
    final File file = new File(installDir, "etc/config.properties");
    String properties = FileUtils.readFileToString(file, "UTF-8");

    // Very rarely the legacy tests attempt to access "mvn:" URLs before the handler has been configured
    // which causes a startup exception. As a workaround we disable the 'requireConfigAdminConfig' check
    // so "mvn:" URLs will always work (they'll just use their default config until the proper config is
    // delivered by configAdmin+fileInstall). So far this has only been observed when running embedded
    // tests, if it ever occurs with the modern tests then we might want to consider making this change
    // in our custom distribution.
    properties = properties.replaceFirst(
        "(?m)^org.ops4j.pax.url.mvn.requireConfigAdminConfig=true",
        "#org.ops4j.pax.url.mvn.requireConfigAdminConfig=true"
    );

    FileUtils.writeStringToFile(file, properties, "UTF-8");
  }

  private void tamperJettyConfiguration() throws IOException {
    final File file = new File(installDir, "etc/jetty.xml");
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

    File confDir = new File(installDir, "etc");
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
    checkState(launcher == null, "Nexus already started");

    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(bootRealm);
    String[] args = {};

    try {
      System.getProperties().putAll(overrides);

      // capture boot/launcher logging per-test
      File karafLog = new File(installDir, "../../logs/" + testId + "/karaf.log");
      System.setProperty("karaf.log", karafLog.getCanonicalPath());

      log.info("Starting Nexus[{}]", testId);
      launcher = launcherFactory.newInstance((Object) args);
      launcherClass.getMethod("launch").invoke(launcher);
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public void stopNexus() throws Exception {
    try {
      log.info("Stopping Nexus");

      if (launcher != null) {
        launcherClass.getMethod("destroy").invoke(launcher);
      }
      launcher = null;
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
      try {
        // The JVM caches URLs along with their current URL handler in a couple of static maps.
        // This causes unexpected issues when restarting legacy tests (even when using isolated
        // classloaders) because the cached handler persists across the restart and still refers
        // to the now shutdown framework. Felix has a few tricks to workaround this, but these
        // are defeated by the isolated legacy test classloader as the new framework cannot see
        // the old handler classes to reflectively update them.

        // (the other solution would be to not shutdown the framework when running legacy tests,
        // this would keep the old URL handlers alive at the cost of a few additional resources)

        Class<?> jarFileFactoryClass = Class.forName("sun.net.www.protocol.jar.JarFileFactory");
        Field fileCacheField = jarFileFactoryClass.getDeclaredField("fileCache");
        Field urlCacheField = jarFileFactoryClass.getDeclaredField("urlCache");
        fileCacheField.setAccessible(true);
        urlCacheField.setAccessible(true);
        ((Map<?, ?>) fileCacheField.get(null)).clear();
        ((Map<?, ?>) urlCacheField.get(null)).clear();
      } catch (Exception e) {
        log.warn("Unable to clear URL cache", e);
      }

      Thread.yield();
      System.gc();
    }
  }
}
