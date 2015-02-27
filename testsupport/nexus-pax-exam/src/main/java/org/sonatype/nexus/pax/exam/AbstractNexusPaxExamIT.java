/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.pax.exam;

import java.io.File;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.sonatype.nexus.configuration.ApplicationDirectories;
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule;
import org.sonatype.sisu.litmus.testsupport.junit.TestIndexRule;
import org.sonatype.sisu.litmus.testsupport.port.PortRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenUrlReference;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.vmOptions;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.doNotModifyLogConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.useOwnKarafExamSystemConfiguration;

/**
 * Abstract class for testing Nexus distributions, test classes can inject any component from the distribution. <br>
 * <br>
 * Extend this class and choose the base distribution (and any optional plugins) that you want to test against:
 * 
 * <pre>
 * &#064;Configuration
 * public static Option[] config() {
 *   return options( //
 *       nexusDistribution(&quot;org.sonatype.nexus.assemblies&quot;, &quot;nexus-base-template&quot;), //
 *       nexusPlugin(&quot;org.sonatype.nexus.plugins&quot;, &quot;nexus-yum-repository-plugin&quot;) //
 *   );
 * }
 * </pre>
 * 
 * @since 3.0
 */
@RunWith(PaxExam.class)
public abstract class AbstractNexusPaxExamIT
{
  private static final String BASEDIR = new File(System.getProperty("basedir", "")).getAbsolutePath();

  public static final String NEXUS_PAX_EXAM_TIMEOUT_KEY = "nexus.pax.exam.timeout";

  public static final int NEXUS_PAX_EXAM_TIMEOUT_DEFAULT = 180000;

  // -------------------------------------------------------------------------

  @Rule
  public final TestDataRule testData = new TestDataRule(resolveBaseFile("src/test/it-resources"));

  @Rule
  public final TestIndexRule testIndex = new TestIndexRule(resolveBaseFile("target/it-reports"),
      resolveBaseFile("target/it-data"));

  protected static final PortRegistry portRegistry = new PortRegistry();

  @Inject
  protected ApplicationDirectories applicationDirectories;

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

  // -------------------------------------------------------------------------

  /**
   * @return Pax-Exam option to install a Nexus distribution based on groupId and artifactId
   */
  public static Option nexusDistribution(final String groupId, final String artifactId) {
    return nexusDistribution(maven(groupId, artifactId).versionAsInProject().type("zip"));
  }

  /**
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
      localRepo = "${maven.repo.local}";
    }

    return composite(

        vmOptions("-Xmx400m", "-XX:MaxPermSize=192m"), // taken from testsuite config

        systemProperty("basedir").value(BASEDIR),

        karafDistributionConfiguration() //
            .karafVersion("3") //
            .frameworkUrl(frameworkZip) //
            .unpackDirectory(resolveBaseFile("target/it-data")) //
            .useDeployFolder(false), //

        configureConsole().ignoreLocalConsole(), // no need for console

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

        // move work directory inside unpacked distribution
        editConfigurationFilePut("etc/nexus.properties", //
            "nexus-work", "${nexus-base}/sonatype-work/nexus"),

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
   * @return Pax-Exam option to install custom invoker factory that waits for Nexus to start
   */
  private static Option nexusPaxExam() {
    final String version = MavenUtils.getArtifactVersion("org.sonatype.nexus", "nexus-pax-exam");
    Option result = mavenBundle("org.sonatype.nexus", "nexus-pax-exam", version);

    final File nexusPaxExam = resolveBaseFile("target/nexus-pax-exam-" + version + ".jar");
    if (nexusPaxExam.isFile()) {
      // when freshly built bundle of 'nexus-pax-exam' is available, copy it over to distribution's system repository
      final String systemPath = "system/org/sonatype/nexus/nexus-pax-exam/" + version + "/" + nexusPaxExam.getName();
      result = composite(replaceConfigurationFile(systemPath, nexusPaxExam), result);
    }

    return result;
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
    testIndex.recordAndCopyLink("nexus.log", resolveWorkFile("logs/nexus.log"));
    testIndex.recordAndCopyLink("request.log", resolveWorkFile("logs/request.log"));

    final String surefirePrefix = "target/surefire-reports/" + getClass().getName();
    testIndex.recordLink("surefire result", resolveBaseFile(surefirePrefix + ".txt"));
    testIndex.recordLink("surefire output", resolveBaseFile(surefirePrefix + "-output.txt"));

    final String failsafePrefix = "target/failsafe-reports/" + getClass().getName();
    testIndex.recordLink("failsafe result", resolveBaseFile(failsafePrefix + ".txt"));
    testIndex.recordLink("failsafe output", resolveBaseFile(failsafePrefix + "-output.txt"));
  }
}
