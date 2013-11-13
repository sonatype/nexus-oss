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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The simplified Nexus booter class, that boots Nexus (the IT test subject) in completely same way as it boots in
 * bundle, but in this same JVM in an isolated classloader, hence, even it will exist in same JVM, REST API is the only
 * possible "contact" with it. Naturally, Java Service Wrapper is not present, but it uses the same Jetty8 class used
 * by
 * bundle boot procedure too. Currently, nexus is started/stopped per test class.
 * <p>
 * There are few trickeries happening here (think ClassLoaders) that will mostly go away once we start producing clean
 * WAR instead of this "bastardized" WAR layout. There are two reasons for this class loader trickery: a) for every
 * Nexus boot, we create new Nexus isolated classloader, but we still keep one "shared" classloader as parent of it,
 * that is never recreated. For reasons, see comments in {@link #tamperJarsForSharedClasspath(File)} method. b) we are
 * emulating what is happening during bundle boot, and since Nexus -- while it is fully fledged Java Web Application --
 * there is one outstanding exception: the JARs are not in /WEB-INF/lib, but in a folder above /WEB-INF, which is
 * illegal. Hence, we, in same way as booter, add the Nexus constituent JARs to a classpath, and let Jetty8 create
 * WebAppClassloader, that will "delegate" to classes, but filtering out Jetty implementation classes on the way.
 * <p>
 * Again, once we start following conventions, this class would be simplified too! For example, the IT-realm and
 * trickery around it would become not needed at all, since Jetty8 would create WebAppClassloader anyway, lifting
 * everything from /WEB-INF/lib directory.
 *
 * @author cstamas
 */
public class Jetty8NexusBooter
    implements NexusBooter
{
  protected static Logger log = LoggerFactory.getLogger(Jetty8NexusBooter.class);

  // ==

  private static final String IT_REALM_ID = "it-realm";

  // ==

  /**
   * The location where Nexus bundle -- the test subject -- is unpacked by nexus-test-environment-maven-plugin
   */
  private final File bundleBasedir;

  /**
   * The folder from where will be "raise" JARs out of bundle, to pin them into the IT-shared classloader.
   */
  private final File sharedLibs;

  /**
   * The ClassWorld we use to create and maintain ClassRealms.
   */
  private final ClassWorld world;

  /**
   * The shared class loader, that contains the JARs from sharedLibs folder and serves as "parent" classloader of
   * IT-realm. This realm is created only once and is reused during Nexus starts/stops.
   */
  private final ClassRealm sharedClassloader;

  // ==

  /**
   * The IT-realm, classloader that contains the Nexus JAR constituents (and other classpath elements, as /conf
   * folder
   * for example). Is recreated per-start and destroyed per-stop.
   */
  private ClassRealm nexusClassloader;

  /**
   * Reference to Jetty8 class instance, got and manipulated using Reflection.
   */
  private Object jetty8;

  /**
   * Creates a NexusBooter instance, that pre-configures and adapts the Nexus bundle unzipped in passed in
   * {@code bundleBasedir} folder. Also, it sets Jetty's port to passed in {@code port}. Warning: the unzipped bundle
   * is modified after this constructor is executed, and WILL NOT BE ABLE TO BOOT anymore using the "standard way"
   * (cd-ing into it's /bin/jsw/... dir and using JSW scripts)! This is due to the fact that this booter moves some
   * JARs out of bundle to make them shared across boots.
   */
  public Jetty8NexusBooter(final File bundleBasedir, final int port)
      throws Exception
  {
    this.bundleBasedir = bundleBasedir.getCanonicalFile();
    log.info("Bundle base directory: {}", bundleBasedir);

    this.sharedLibs = new File(bundleBasedir.getParentFile(), "shared");
    log.info("Shared library directory: {}", sharedLibs);

    // modify the properties
    tamperJettyConfiguration(bundleBasedir, port);

    // shuffle bundle files
    tamperJarsForSharedClasspath(bundleBasedir);

    // --------------
    // Setting system props, even if it might be redundant, just to be 100% positive
    // --------------
    // set system property for bundleBasedir
    System.setProperty("bundleBasedir", bundleBasedir.getAbsolutePath());
    // needed since NEXUS-4515
    System.setProperty("jettyContext", "nexus.properties");
    System.setProperty("jettyPlexusCompatibility", "true");

    // Configure bootstrap logback configuration
    System.setProperty("logback.configurationFile", new File(bundleBasedir, "conf/logback.xml").getAbsolutePath());

    // guice finalizer
    System.setProperty("guice.executor.class", "NONE");

    // Making MI integration in Nexus behave in-sync
    System.setProperty("org.sonatype.nexus.events.IndexerManagerEventInspector.async", Boolean.FALSE.toString());

    // Note: autorouting initialization prevented
    // Presence breaks many ITs, especially those that either listen for proxy requests (will be more coz of prefix file
    // and scrape discovery), or because remote proxy setup happens after nexus boot, and autorouting discovery makes proxies autoblock.
    // In either case, IT working with autorouting should explicitly enable it.
    // As "legacy" ITs are coming anyway from pre-WL era, they will have autorouting disabled ALWAYS
    // To write IT covering WL you'd use anyway the "new" IT infrastructure instead of this.
    System.setProperty(ConfigImpl.FEATURE_ACTIVE_KEY, Boolean.FALSE.toString());

    // ---------------

    // create ClassWorld
    world = new ClassWorld();

    // create shared loader
    sharedClassloader = buildSharedClassLoader();
  }

  /**
   * Starts one instance of Nexus bundle in an isolated class loader. May be invoked only once, or after
   * {@link #stopNexus()} is invoked only, otherwise will throw IllegalStateException.
   */
  public void startNexus(final String testId)
      throws Exception
  {
    if (jetty8 != null) {
      // 2nd invocation? Stop first or puke?
      throw new IllegalStateException("Nexus already started!");
    }

    // create classloader
    nexusClassloader = buildNexusClassLoader(bundleBasedir, testId);

    final ClassLoader original = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(sharedClassloader);

      // we have to get it from shared, as it is present here too, but by mistake it seems
      final Class<?> appContextClass = sharedClassloader.loadClass("org.sonatype.appcontext.AppContext");
      final Class<?> jetty8Class = sharedClassloader.loadClass("org.sonatype.sisu.jetty.Jetty8");

      log.info("Starting Nexus[{}]", testId);

      jetty8 =
          jetty8Class.getConstructor(File.class, ClassLoader.class, appContextClass, Map[].class).newInstance(
              new File(bundleBasedir, "conf/jetty.xml"), nexusClassloader, null,
              new Map[]{defaultContext(bundleBasedir)});
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }

    jetty8.getClass().getMethod("startJetty").invoke(jetty8);
  }

  /**
   * Stops, and cleans up the started Nexus instance. May be invoked any times, it will NOOP if not needed to do
   * anything. Will try to ditch the used classloader. The {@link #clean()} method will be invoked on every
   * invocation
   * of this method, making it more plausible for JVM to recover/GC all the stuff from memory in case of any glitch.
   */
  public void stopNexus()
      throws Exception
  {
    try {
      log.info("Stopping Nexus");

      if (jetty8 != null) {
        jetty8.getClass().getMethod("stopJetty").invoke(jetty8);
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

  // == Protected methods below

  /**
   * Builds minimal "default" context to boot Jetty8 properly. In case of bundle, the {@code bundleBasedir} context
   * element is got from Java System Properties, and is set by Java Service Wrapper (in wrapper.conf).
   */
  protected Map<String, String> defaultContext(final File bundleBasedir) {
    Map<String, String> ctx = new HashMap<String, String>();
    ctx.put("bundleBasedir", bundleBasedir.getAbsolutePath());
    return ctx;
  }

  /**
   * Builds a ClassRealm out from contents of the {@link #sharedLibs} folder. Some JARs from bundle (/lib and some
   * plugin dependencies) are moved here to make them "shared" across executions.
   */
  protected ClassRealm buildSharedClassLoader()
      throws Exception
  {
    List<URL> urls = new ArrayList<URL>();

    final File[] jars = sharedLibs.listFiles();

    for (File jar : jars) {
      urls.add(jar.toURI().toURL());
    }

    ClassRealm realm = world.newRealm("it-shared", null);

    log.info("Shared ClassPath:");
    for (URL url : urls) {
      log.info("  {}", url);
      realm.addURL(url);
    }

    return realm;
  }

  /**
   * Builds a ClassRealm in same way as Nexus Bundle does: it adds /conf folder plus, all the JARs from the /lib
   * folder into class loader. This realm is built and thrown away per one execution, and every execution creates a
   * new one, guaranteeing that even class static members are in new/clean state. The created realm ID will be of
   * form
   * "it-realm" + testId for easier debug/heap dump analysis.
   */
  protected ClassRealm buildNexusClassLoader(final File bundleBasedir, final String testId)
      throws Exception
  {
    List<URL> urls = new ArrayList<URL>();

    final File libDir = new File(bundleBasedir, "lib");

    final File[] jars = libDir.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".jar");
      }
    });

    for (File jar : jars) {
      urls.add(jar.toURI().toURL());
    }

    ClassRealm realm = world.newRealm(IT_REALM_ID + "-" + testId, sharedClassloader);

    log.info("Nexus ClassPath:");
    for (URL url : urls) {
      log.info("  {}", url);
      realm.addURL(url);
    }

    return realm;
  }

  /**
   * Modifies the Jetty's configuration files (those used by Jetty8 class, that is used in bundle but also here to
   * boot Jetty8). It sets Jetty port to the one wanted by IT, but also eliminates the appearance of Jetty's
   * "shutdown thread" that usually "pins" the it-realm classloader to memory, making it not garbage collected, and
   * making ITs OOM PermGen.
   */
  protected void tamperJettyConfiguration(final File basedir, final int port)
      throws IOException
  {
    // ==
    // Set the port to the one expected by IT
    {
      final File jettyProperties = new File(basedir, "conf/nexus.properties");

      if (!jettyProperties.isFile()) {
        throw new FileNotFoundException("Jetty properties not found at " + jettyProperties.getAbsolutePath());
      }

      Properties p = new Properties();
      try (InputStream in = new FileInputStream(jettyProperties)) {
        p.load(in);
      }

      p.setProperty("application-port", String.valueOf(port));

      try (OutputStream out = new FileOutputStream(jettyProperties)) {
        p.store(out, "NexusStatusUtil");
      }
    }

    // ==
    // Disable the shutdown hook, since it disturbs the embedded work
    // In Jetty8, any invocation of server.stopAtShutdown(boolean) will create a thread in a class static member.
    // Hence, we simply want to make sure, that there is NO invocation happening of that method.
    {
      final File jettyXml = new File(basedir, "conf/jetty.xml");

      if (!jettyXml.isFile()) {
        throw new FileNotFoundException("Jetty properties not found at " + jettyXml.getAbsolutePath());
      }

      String jettyXmlString = FileUtils.readFileToString(jettyXml, "UTF-8");

      // was: we just set the value to "false", but the server.stopAtShutdown() invocation still happened,
      // triggering thread to be created in static member
      // jettyXmlString =
      // jettyXmlString.replace( "Set name=\"stopAtShutdown\">true", "Set name=\"stopAtShutdown\">false" );

      // new: completely removing the server.stopAtShutdown() method invocation, to try to prevent thread
      // creation at all
      jettyXmlString =
          jettyXmlString.replace("<Set name=\"stopAtShutdown\">true</Set>",
              "<!-- NexusBooter: Set name=\"stopAtShutdown\">true</Set-->");

      // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357318#c62
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        jettyXmlString =
            jettyXmlString.replace("org.sonatype.nexus.bootstrap.jetty.InstrumentedSelectChannelConnector",
                "org.eclipse.jetty.server.nio.BlockingChannelConnector");
      }

      FileUtils.writeStringToFile(jettyXml, jettyXmlString, "UTF-8");
    }
  }

  /**
   * This method "lifts" some JARs from Nexus bundle (/lib and also plugin dependencies folders) into
   * {@link #sharedLibs} folder. For reasons per-entry, see method body comments.
   */
  protected void tamperJarsForSharedClasspath(final File basedir)
      throws IOException
  {
    // Explanation, we filter for lucene-*.jar files that are bigger than one byte, in all directories below
    // bundleBasedir, since we
    // have to move them into /shared newly created folder to set up IT shared classpath.
    // But, we have to make it carefully, since we might be re-created during multi-forked ITs but the test-env
    // plugin unzips the nexus bundle only once
    // at the start of the build. So, we have to check and do it only once.
    // cstamas: Lucene is no more in core, not needed to do this anymore!
    // tamperJarsForSharedClasspath( basedir, sharedLibs, "lucene-*.jar" );

    // LDAP does not unregister it? Like SISU container does not invoke Disposable.dispose() to make patch for
    // Provider unregistration happen? NO: the cause is if someone creates HTTPS connection while BC is registered,
    // JCEs SSLSocketFactory will get a grab on it. So, SISU is not faulty here, unregistration does happen, but
    // the URLConnection instance may still exists. So, we are lifting the provider into "shareds", and registering
    // it manually. LDAP's DefaultPlexusCipher obeys the registration rules, so will happily live with BC
    // registered.
    // WE ARE NOT REGISTERING IT ANYMORE, but is left here at "hand"
    // Fixed with NEXUS-4443
    // tamperJarsForSharedClasspath( basedir, sharedLibs, "bcprov-*.jar" );

    // logback
    // tamperJarsForSharedClasspath( basedir, sharedLibs, "slf4j-*.jar" );
    // tamperJarsForSharedClasspath( basedir, sharedLibs, "logback-*.jar" );

    // move jetty (actually, all that is level up in real bundle too) level up, it is isolated anyway in real bundle
    tamperJarsForSharedClasspath(basedir, sharedLibs, "jetty-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "javax.servlet-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "nexus-jetty8-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "plexus-utils-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "plexus-interpolation-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "plexus-classworlds-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "nexus-appcontext-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "slf4j-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "logback-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "nexus-logging-extras-appender-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "metrics-core-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "metrics-jetty-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "metrics-logback-*.jar");
    tamperJarsForSharedClasspath(basedir, sharedLibs, "nexus-bootstrap-*.jar");
  }

  /**
   * This method just the "heavy lifting" for method {@link #tamperJarsForSharedClasspath(File)}. It traverses passed
   * in {@code basedir}, matches against {@code wildcard}, and matched JARs moves to {@code sharedLibs} folder while
   * keeping source files but overwriting them to 0 byte length "placeholders". The reason for this is, that in case
   * of plugin dependency, Nexus Plugin Manager would "scream" if dependency is declared by it's corresponding JAR
   * file is not found. Having the 0 byte "fake" file in place, we only make Nexus PM "happy", but effectively
   * removing classes from Nexus core or plugin's classloader, since they will be in the IT shared classloader and is
   * visible to them.
   */
  protected void tamperJarsForSharedClasspath(final File basedir, final File sharedLibs, final String wildcard)
      throws IOException
  {
    @SuppressWarnings("unchecked")
    Collection<File> files =
        (Collection<File>) FileUtils.listFiles(basedir, new WildcardFileFilter(wildcard), TrueFileFilter.TRUE);

    for (File file : files) {
      // only if not in /shared folder and not zeroed already
      if (!file.getParentFile().equals(sharedLibs) && file.length() > 0) {
        // copy jar to /shared
        FileUtils.copyFile(file, new File(sharedLibs, file.getName()));

        // replace jar with dummies (to make Nexus Plugin Manager happy) and prevent it's classes to be
        // loaded from non-shared class loader
        FileUtils.writeStringToFile(file, "");
      }
    }
  }

  /**
   * Cleans the references to IT-realm, making it garbage collectable (naturally, this can be only "best effort").
   */
  protected void clean() {
    if (nexusClassloader != null) {
      try {
        world.disposeRealm(nexusClassloader.getId());
      }
      catch (NoSuchRealmException e) {
        // huh?
      }
    }

    // drop references
    this.jetty8 = null;
    this.nexusClassloader = null;

    // give some relief for other (like JVM internal) threads
    Thread.yield();

    // force GC, may help
    System.gc();
  }
}
