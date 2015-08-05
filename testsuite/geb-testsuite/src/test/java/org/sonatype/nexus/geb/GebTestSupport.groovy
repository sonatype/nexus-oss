/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.geb

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy
import org.ops4j.pax.exam.spi.reactors.PerClass

import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import javax.inject.Inject
import javax.inject.Named
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

import org.sonatype.nexus.common.app.ApplicationDirectories
import org.sonatype.nexus.common.event.EventSubscriberHost
import org.sonatype.nexus.geb.traits.ExtJS
import org.sonatype.nexus.scheduling.TaskScheduler
import org.sonatype.sisu.goodies.common.Loggers
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule
import org.sonatype.sisu.litmus.testsupport.junit.TestIndexRule
import org.sonatype.sisu.litmus.testsupport.port.PortRegistry

import com.google.common.base.Joiner
import com.google.common.base.Throwables
import com.google.common.util.concurrent.Uninterruptibles
import geb.ConfigurationLoader
import geb.junit4.GebReportingTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.ops4j.net.URLUtils
import org.ops4j.pax.exam.Configuration
import org.ops4j.pax.exam.CoreOptions
import org.ops4j.pax.exam.MavenUtils
import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.OptionUtils
import org.ops4j.pax.exam.junit.PaxExam
import org.ops4j.pax.exam.junit.PaxExamServer
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationFileExtendOption
import org.ops4j.pax.exam.options.MavenUrlReference
import org.ops4j.pax.exam.options.ProvisionOption
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode
import org.slf4j.Logger

import static com.google.common.base.Preconditions.checkNotNull
import static org.ops4j.pax.exam.CoreOptions.composite
import static org.ops4j.pax.exam.CoreOptions.maven
import static org.ops4j.pax.exam.CoreOptions.mavenBundle
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty
import static org.ops4j.pax.exam.CoreOptions.systemProperty
import static org.ops4j.pax.exam.CoreOptions.systemTimeout
import static org.ops4j.pax.exam.CoreOptions.vmOptions
import static org.ops4j.pax.exam.CoreOptions.when
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
//class TestSpec extends GebReportingSpec // TODO - KR would need a pax-exam compatible running for Spock Sputnik here
class GebTestSupport
    extends GebReportingTest implements ExtJS
{

  public static final String BASEDIR = new File(System.getProperty("basedir", "")).getAbsolutePath();

  public static final String NEXUS_PAX_EXAM_TIMEOUT_KEY = "nexus.pax.exam.timeout";

  public static final int NEXUS_PAX_EXAM_TIMEOUT_DEFAULT = 300000;

  public static final int NEXUS_TEST_START_LEVEL = 200;

  // -------------------------------------------------------------------------

  @Rule
  public final TestDataRule testData = new TestDataRule(resolveBaseFile("src/test/it-resources"));

  @Rule
  public final TestIndexRule testIndex = new TestIndexRule(resolveBaseFile("target/it-reports"),
      resolveBaseFile("target/it-data"));

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  public static final PortRegistry portRegistry = new PortRegistry();

  @Inject
  @Named('http://127.0.0.1:${application-port}${nexus-context-path}')
  protected URL nexusUrl;

  @Inject
  @Named('https://127.0.0.1:${application-port-ssl}${nexus-context-path}')
  protected URL nexusSecureUrl;

  @Inject
  protected ApplicationDirectories applicationDirectories;

  @Inject
  protected EventSubscriberHost eventSubscriberHost;

  @Inject
  protected TaskScheduler taskScheduler;

  protected final Logger log = checkNotNull(createLogger());

  protected Logger createLogger() {
    return Loggers.getLogger(this);
  }

  @Before
  void setup() {
    System.setProperty("geb.build.baseUrl", nexusUrl.toString())
  }
  
  @BeforeClass
  static void setupSpec() {
    System.setProperty('geb.env', System.getProperty('geb.env', 'phantom'))
  }

  @Configuration
  public static Option[] configureNexus() {
    return options(
        nexusDistribution('org.sonatype.nexus.assemblies', 'nexus-base-template'),

        // Add testsuite dependencies as bundles

        mavenBundle('org.codehaus.groovy', 'groovy-all').versionAsInProject(),
        mavenBundle('org.apache.commons', 'commons-exec').versionAsInProject(),

        // Attach GEB code to nexus-pax-exam

        wrappedBundle(maven('org.gebish', 'geb-core').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.gebish', 'geb-ast').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.gebish', 'geb-waiting').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.gebish', 'geb-exceptions').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.gebish', 'geb-implicit-assertions').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.gebish', 'geb-junit4').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),

        // Attach Selenium code to nexus-pax-exam

        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-api').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-support').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-remote-driver').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-firefox-driver').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-chrome-driver').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('org.seleniumhq.selenium', 'selenium-safari-driver').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('io.netty','netty').versionAsInProject()) //selenium-safari-driver needs this
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),
        wrappedBundle(maven('com.codeborne','phantomjsdriver').versionAsInProject())
            .instructions('Fragment-Host=org.sonatype.nexus.pax-exam'),

        // FIXME: This does not take effect unless you do *NOT* have NEXUS_RESOURCE_DIRS set
        // FIXME: Needs to include more resource-dirs as well to pick up changes
        editConfigurationFileExtend('etc/system.properties',
            'nexus.resource.dirs', resolveBaseFile('src/test/ft-resources').absolutePath
        ),
        editConfigurationFileExtend('etc/system.properties',
            'geb.env', System.getProperty('geb.env', 'phantom')
        ),
        editConfigurationFileExtend('etc/system.properties',
            'geb.workdir', resolveBaseFile('target').absolutePath
        ),
    );
  }
  // -------------------------------------------------------------------------

  /**
   * Resolves path against the basedir of the surrounding Maven project.
   */
  public static File resolveBaseFile(final String path) {
    return Paths.get(BASEDIR, path).toFile();
  }

  /**
   * Resolves path by searching the it-resources of the Maven project.
   *
   * @see TestDataRule#resolveFile(String)
   */
  public File resolveTestFile(final String path) {
    return testData.resolveFile(path);
  }

  /**
   * Resolves path against the Nexus application directory.
   */
  public File resolveAppFile(final String path) {
    return new File(applicationDirectories.getAppDirectory(), path);
  }

  /**
   * Resolves path against the Nexus work directory.
   */
  public File resolveWorkFile(final String path) {
    return new File(applicationDirectories.getWorkDirectory(), path);
  }

  /**
   * Resolves path against the Nexus temp directory.
   */
  public File resolveTempFile(final String path) {
    return new File(applicationDirectories.getTemporaryDirectory(), path);
  }

  /**
   * Resolves path against the given URL.
   */
  public static URL resolveUrl(final URL url, final String path) {
    try {
      return URI.create(url + "/" + path).normalize().toURL();
    }
    catch (final Exception e) {
      throw Throwables.propagate(e);
    }
  }

  // -------------------------------------------------------------------------

  /**
   * Periodically polls function until it returns {@code true} or 30 seconds have elapsed.
   *
   * @throws InterruptedException if the thread is interrupted or the timeout exceeded
   */
  public static void waitFor(final Callable<Boolean> function) throws InterruptedException {
    waitFor(function, 30000);
  }

  /**
   * Periodically polls function until it returns {@code true} or the timeout has elapsed.
   *
   * @throws InterruptedException if the thread is interrupted or the timeout exceeded
   */
  public static void waitFor(final Callable<Boolean> function, final long millis) throws InterruptedException {
    Thread.yield();
    for (int i = 0; i < millis / 100; i++) {
      try {
        if (Boolean.TRUE.equals(function.call())) {
          return; // success
        }
        Thread.sleep(100);
      }
      catch (final InterruptedException e) {
        throw e; // cancelled
      }
      catch (final Exception e) {
        Thread.sleep(100);
      }
    }
    throw new InterruptedException();
  }

  /**
   * @return Function that returns {@code true} when there's a response from the URL; otherwise {@code false}
   */
  public static Callable<Boolean> responseFrom(final URL url) {
//    return new Callable<Boolean>()
    return {
//      public Boolean call() throws Exception {
        HttpURLConnection conn = null;
        try {
          conn = (HttpURLConnection) url.openConnection();
          if (conn instanceof HttpsURLConnection) {
            // relax host and certificate checks as we just want to see if it's up
            ((HttpsURLConnection) conn).setHostnameVerifier({true} as HostnameVerifier);
            URLUtils.prepareForSSL(conn);
          }
          conn.setRequestMethod("HEAD");
          conn.connect();
          return true;
        }
        finally {
          if (conn != null) {
            conn.disconnect();
          }
        }
//      }
    } as Callable<Boolean>
  }

  /**
   * @return Function that returns {@code true} when the event system is calm; otherwise {@code false}
   */
  protected Callable<Boolean> calmPeriod() {
//    return new Callable<Boolean>()
    return {
//      public Boolean call() throws Exception {
        eventSubscriberHost.isCalmPeriod();
//      }
    } as Callable<Boolean>;
  }

  /**
   * @return Function that returns {@code true} when all tasks have stopped; otherwise {@code false}
   */
  protected Callable<Boolean> tasksDone() {
    Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
//    return new Callable<Boolean>()
    return {
//      public Boolean call() throws Exception {
        return taskScheduler.getRunningTaskCount() == 0;
      } as Callable<Boolean>
//    };
  }

  // -------------------------------------------------------------------------

  /**
   * To test a different version set the 'it.nexus.bundle.version' system property.<br>
   * You can also override the 'groupId', 'artifactId', and 'classifier' the same way.
   *
   * @return Pax-Exam option to install a Nexus distribution based on groupId and artifactId
   */
  public static Option nexusDistribution(final String groupId, final String artifactId) {
    return nexusDistribution(maven(groupId, artifactId).versionAsInProject().type("zip"));
  }

  /**
   * To test a different version set the 'it.nexus.bundle.version' system property.<br>
   * You can also override the 'groupId', 'artifactId', and 'classifier' the same way.
   *
   * @return Pax-Exam option to install a Nexus distribution based on groupId, artifactId and classifier
   */
  public static Option nexusDistribution(final String groupId, final String artifactId, final String classifier) {
    return nexusDistribution(maven(groupId, artifactId).classifier(classifier).versionAsInProject().type("zip"));
  }

  /**
   * @return Pax-Exam option to install a Nexus distribution from the given framework zip
   */
  public static Option nexusDistribution(final MavenUrlReference frameworkZip) {

    // support explicit CI setting as well as automatic detection
    String localRepo = System.getProperty("maven.repo.local", "");
    if (localRepo.length() > 0) {
      // pass on explicit setting to Pax-URL (otherwise it uses wrong value)
      System.setProperty("org.ops4j.pax.url.mvn.localRepository", localRepo);
    }
    else {
      // use placeholder in karaf config
      localRepo = '${maven.repo.local}';
    }

    // allow overriding the distribution under test from the command-line
    if (System.getProperty("it.nexus.bundle.groupId") != null) {
      frameworkZip.groupId(System.getProperty("it.nexus.bundle.groupId"));
    }
    if (System.getProperty("it.nexus.bundle.artifactId") != null) {
      frameworkZip.artifactId(System.getProperty("it.nexus.bundle.artifactId"));
    }
    if (System.getProperty("it.nexus.bundle.version") != null) {
      frameworkZip.version(System.getProperty("it.nexus.bundle.version"));
    }
    if (System.getProperty("it.nexus.bundle.classifier") != null) {
      frameworkZip.classifier(System.getProperty("it.nexus.bundle.classifier"));
    }

    boolean debugging = Boolean.parseBoolean(System.getProperty("it.debug"));

    // allow overriding of the out-of-the-box logback configuration
    File logbackProperties = resolveBaseFile("target/test-classes/logback.properties");
    File logbackNexusXml = resolveBaseFile("target/test-classes/logback-nexus.xml");

    return composite(

        vmOptions("-Xmx400m", "-XX:MaxPermSize=192m"), // taken from testsuite config

        systemTimeout(getExamTimeout()),

        propagateSystemProperty(NEXUS_PAX_EXAM_TIMEOUT_KEY),

        systemProperty("basedir").value(BASEDIR),

        karafDistributionConfiguration() //
            .karafVersion("3") //
            .frameworkUrl(frameworkZip) //
            .unpackDirectory(resolveBaseFile("target/it-data")) //
            .useDeployFolder(false), //

        when(debugging).useOptions(debugConfiguration()), // port 5005, suspend=y

        configureConsole().ignoreLocalConsole().ignoreRemoteShell(), // no need for console

        doNotModifyLogConfiguration(), // don't mess with our logging

        keepRuntimeFolder(), // keep files around in case we need to debug

        editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", // so pax-exam can fetch its feature
            "org.ops4j.pax.url.mvn.repositories", "https://repo1.maven.org/maven2@id=central"),

        editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", // so we can fetch local snapshots
            "org.ops4j.pax.url.mvn.localRepository", localRepo),

        useOwnKarafExamSystemConfiguration("nexus"),

        nexusPaxExam(), // registers invoker factory that waits for nexus to start before running tests

        // merge hamcrest-library extras with the core hamcrest bundle from Pax
        wrappedBundle(maven("org.hamcrest", "hamcrest-library").versionAsInProject()) //
            .instructions("Fragment-Host=org.ops4j.pax.tipi.hamcrest.core"),

        when(logbackProperties.canRead()).useOptions( //
            replaceConfigurationFile("data/etc/logback.properties", logbackProperties)),
        when(logbackNexusXml.canRead()).useOptions( //
            replaceConfigurationFile("data/etc/logback-nexus.xml", logbackNexusXml)),

        // randomize ports...
        editConfigurationFilePut("etc/nexus.properties", //
            "application-port", Integer.toString(portRegistry.reservePort())),
        editConfigurationFilePut("etc/nexus.properties", //
            "application-port-ssl", Integer.toString(portRegistry.reservePort())),
        editConfigurationFilePut("etc/org.apache.karaf.management.cfg", //
            "rmiRegistryPort", Integer.toString(portRegistry.reservePort())),
        editConfigurationFilePut("etc/org.apache.karaf.management.cfg", //
            "rmiServerPort", Integer.toString(portRegistry.reservePort()))

    );
  }

  /**
   * @return Pax-Exam option to change the context path for the Nexus distribution
   */
  public static Option withContextPath(final String contextPath) {
    return editConfigurationFilePut("etc/nexus.properties", "nexus-context-path", contextPath);
  }

  /**
   * @return Pax-Exam option to enable HTTPS support in the Nexus distribution
   */
  public static Option withHttps(final File keystore) {
    return composite(
        editConfigurationFileExtend("etc/custom.properties", "nexus-args", '${karaf.base}/etc/jetty-https.xml'),
        replaceConfigurationFile("etc/ssl/keystore.jks", keystore));
  }

  /**
   * @return Pax-Exam option to install a Nexus plugin based on groupId and artifactId
   */
  public static Option nexusPlugin(final String groupId, final String artifactId) {
    return nexusPlugin(maven(groupId, artifactId).versionAsInProject().classifier("features").type("xml"), artifactId);
  }

  /**
   * @return Pax-Exam option to install a Nexus plugin from the given feature XML and name
   */
  public static Option nexusPlugin(final MavenUrlReference featureXml, final String name) {
    return composite(features(featureXml), editConfigurationFileExtend("etc/nexus.properties", "nexus-features", name));
  }

  /**
   * Replacement for {@link CoreOptions#options(Option...)} to workaround Pax-Exam 'feature'
   * where only the last 'editConfigurationFileExtend' for a given property key is honoured.
   */
  public static Option[] options(final Option... options) {
    final List<Option> result = new ArrayList<>();

    final List<String> nexusFeatures = new ArrayList<>();
    for (final Option o : OptionUtils.expand(options)) {

      // filter out the individual nexus-features values
      if (o instanceof KarafDistributionConfigurationFileExtendOption) {
        if ("nexus-features".equals(((KarafDistributionConfigurationFileExtendOption) o).getKey())) {
          nexusFeatures.add(((KarafDistributionConfigurationFileExtendOption) o).getValue());
          continue;
        }
      }
      // provide default start level for any additional test bundles
      else if (o instanceof ProvisionOption<?> && ((ProvisionOption<?>) o).getStartLevel() == null) {
        ((ProvisionOption<?>) o).startLevel(NEXUS_TEST_START_LEVEL);
      }

      result.add(o);
    }

    if (nexusFeatures.size() > 0) {
      // combine the nexus-features values into a single request
      result.add(editConfigurationFileExtend("etc/nexus.properties", //
          "nexus-features", Joiner.on(',').join(nexusFeatures)));
    }

    return result.toArray(new Option[result.size()]);
  }

  /**
   * Processes two sequences of options and combines them into a single sequence.
   */
  public static Option[] options(final Option[] options1, final Option... options2) {
    return options(OptionUtils.combine(options1, options2));
  }

  // -------------------------------------------------------------------------

  @Before
  public void startTestRecording() {
    // Pax-Exam guarantees unique test location, use that with index
    testIndex.setDirectory(applicationDirectories.getAppDirectory());
  }

  @After
  public void stopTestRecording() {
    testIndex.recordAndCopyLink("karaf.log", resolveAppFile("data/log/karaf.log"));
    testIndex.recordAndCopyLink("nexus.log", resolveWorkFile("log/nexus.log"));
    testIndex.recordAndCopyLink("request.log", resolveWorkFile("log/request.log"));

    final String surefirePrefix = "target/surefire-reports/" + getClass().getName();
    testIndex.recordLink("surefire result", resolveBaseFile(surefirePrefix + ".txt"));
    testIndex.recordLink("surefire output", resolveBaseFile(surefirePrefix + "-output.txt"));

    final String failsafePrefix = "target/failsafe-reports/" + getClass().getName();
    testIndex.recordLink("failsafe result", resolveBaseFile(failsafePrefix + ".txt"));
    testIndex.recordLink("failsafe output", resolveBaseFile(failsafePrefix + "-output.txt"));
  }

  // -------------------------------------------------------------------------

  /**
   * @return Pax-Exam option to install custom invoker factory that waits for Nexus to start
   */
  private static Option nexusPaxExam() {
    final String version = MavenUtils.getArtifactVersion("org.sonatype.nexus", "nexus-pax-exam");
    Option result = mavenBundle("org.sonatype.nexus", "nexus-pax-exam", version).startLevel(0);

    final File nexusPaxExam = resolveBaseFile("target/nexus-pax-exam-" + version + ".jar");
    if (nexusPaxExam.isFile()) {
      // when freshly built bundle of 'nexus-pax-exam' is available, copy it over to distribution's system repository
      final String systemPath = "system/org/sonatype/nexus/nexus-pax-exam/" + version + "/" + nexusPaxExam.getName();
      result = composite(replaceConfigurationFile(systemPath, nexusPaxExam), result);
    }

    return result;
  }

  /**
   * @return Timeout to apply when waiting for Nexus to start
   */
  private static int getExamTimeout() {
    try {
      return Integer.parseInt(System.getProperty(NEXUS_PAX_EXAM_TIMEOUT_KEY));
    }
    catch (final Exception e) {
      return NEXUS_PAX_EXAM_TIMEOUT_DEFAULT;
    }
  }
}
