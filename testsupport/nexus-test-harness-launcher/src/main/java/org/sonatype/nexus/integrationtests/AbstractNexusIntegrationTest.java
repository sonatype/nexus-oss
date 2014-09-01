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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.sisu.goodies.prefs.memory.MemoryPreferencesFactory;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data START NOTE,
 * this class is not really abstract so I can work around a the <code>@BeforeClass</code>, <code>@AfterClass</code>
 * issues, this should be refactored a little, but it might be ok, if we switch to TestNg
 */
// @RunWith(ConsoleLoggingRunner.class)
public abstract class AbstractNexusIntegrationTest
  extends TestSupport
{
  public static final String REPO_NEXUS_TEST_HARNESS_RELEASE_GROUP = "nexus-test-harness-release-group";

  public static final String REPO_TEST_HARNESS_REPO = "nexus-test-harness-repo";

  public static final String REPO_TEST_HARNESS_REPO2 = "nexus-test-harness-repo2";

  public static final String REPO_TEST_HARNESS_RELEASE_REPO = "nexus-test-harness-release-repo";

  public static final String REPO_TEST_HARNESS_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

  public static final String REPO_RELEASE_PROXY_REPO1 = "release-proxy-repo-1";

  public static final String REPO_TEST_HARNESS_SHADOW = "nexus-test-harness-shadow";

  protected static boolean NEEDS_INIT = false;

  public static final String REPOSITORY_RELATIVE_URL = "content/repositories/";

  public static final String GROUP_REPOSITORY_RELATIVE_URL = "content/groups/";

  @ClassRule
  public static final ProfilerHelper profilerHelper = new ProfilerHelper();

  private String testRepositoryId;

  public static String nexusBaseDir;

  public static final String nexusBaseUrl;

  public static final String nexusWorkDir;

  public static final String RELATIVE_CONF_DIR = "../sonatype-work/nexus/etc";

  public static final String WORK_CONF_DIR;

  public static final Integer nexusControlPort;

  public static final int nexusApplicationPort;

  protected static final String nexusLogDir;

  protected final Logger log = util.getLog();

  protected static Logger staticLog = LoggerFactory.getLogger(AbstractNexusIntegrationTest.class);

  private static Properties systemPropertiesBackup;

  static {
    // Configure slf4j JUL bridge
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    // Use in-memory preferences
    System.setProperty("java.util.prefs.PreferencesFactory", MemoryPreferencesFactory.class.getName());

    // guice finalizer turned OFF
    System.setProperty("guice.executor.class", "NONE");

    // skip misplaced guice annotation checking, this causes failures due to use of guice annotations on interfaces
    // ... which is commonly done on Nexus ExtensionPoints :-\
    System.setProperty("guice.disable.misplaced.annotation.check", "true");

    // NEXUS-5660 test with ipv4 as app runs with this set now
    System.setProperty("java.net.preferIPv4Stack", "true");
  }

  static {
    nexusApplicationPort = TestProperties.getInteger("nexus.application.port");
    nexusControlPort = TestProperties.getInteger("nexus.control.port");
    nexusBaseDir = TestProperties.getString("nexus.base.dir");
    nexusWorkDir = TestProperties.getString("nexus.work.dir");
    WORK_CONF_DIR = nexusWorkDir + "/etc";
    nexusLogDir = TestProperties.getString("nexus.log.dir");
    nexusBaseUrl = TestProperties.getString("nexus.base.url");

    TestContainer.getInstance().getTestContext().setNexusUrl(nexusBaseUrl);
  }

  // == instance utils

  private static NexusStatusUtil nexusStatusUtil = new NexusStatusUtil();

  protected static NexusStatusUtil getNexusStatusUtil() {
    return nexusStatusUtil;
  }

  private final NexusConfigUtil nexusConfigUtil = new NexusConfigUtil();

  protected NexusConfigUtil getNexusConfigUtil() {
    return nexusConfigUtil;
  }

  private final SecurityConfigUtil securityConfigUtil = new SecurityConfigUtil();

  protected SecurityConfigUtil getSecurityConfigUtil() {
    return securityConfigUtil;
  }

  private final DeployUtils deployUtils = new DeployUtils();

  protected DeployUtils getDeployUtils() {
    return deployUtils;
  }

  private final SearchMessageUtil searchMessageUtil = new SearchMessageUtil();

  protected SearchMessageUtil getSearchMessageUtil() {
    return searchMessageUtil;
  }

  private final EventInspectorsUtil eventInspectorsUtil = new EventInspectorsUtil();

  protected EventInspectorsUtil getEventInspectorsUtil() {
    return eventInspectorsUtil;
  }

  // == Constructors

  protected AbstractNexusIntegrationTest() {
    this("nexus-test-harness-repo");
  }

  protected AbstractNexusIntegrationTest(final String testRepositoryId) {
    // we also need to setup a couple fields, that need to be pulled out of a bundle
    this.testRepositoryId = testRepositoryId;
    // this.nexusTestRepoUrl = baseNexusUrl + REPOSITORY_RELATIVE_URL + testRepositoryId + "/";
  }

  // == Test "lifecycle" (@Before/@After...)

  @BeforeClass
  public static void staticOncePerClassSetUp() throws Exception {
    staticLog.info("Static setup");

    // hacky state machine
    NEEDS_INIT = true;
  }

  /**
   * To me this seems like a bad hack around this problem. I don't have any other thoughts though. <BR/>
   * If you see this and think: "Wow, why did he to that instead of XYZ, please let me know." <BR/>
   * The issue is that we want to init the tests once (to start/stop the app) and the <code>@BeforeClass</code> is
   * static, so we don't have access to the package name of the running tests. We are going to use the package name
   * to
   * find resources for additional setup. NOTE: With this setup running multiple Test at the same time is not
   * possible.
   */
  @Before
  public void oncePerClassSetUp() throws Exception {
    log.info("Instance setup");
    synchronized (AbstractNexusIntegrationTest.class) {
      if (NEEDS_INIT) {
        log.info("Initializing");

        // start per-IT plexus container
        TestContainer.getInstance().startPlexusContainer(getClass(), new SecurityModule());

        systemPropertiesBackup = System.getProperties();

        final String useDebugFor = System.getProperty("it.nexus.log.level.use.debug");
        if (!StringUtils.isEmpty(useDebugFor)) {
          final String[] segments = useDebugFor.split(",");
          for (final String segment : segments) {
            if (getClass().getSimpleName().matches(segment.replace(".", "\\.").replace("*", ".*"))) {
              System.setProperty("it.nexus.log.level", "DEBUG");
            }
          }
        }

        // tell the console what we are doing, now that there is no output its
        String logMessage = "Running Test: " + getTestId() + " - Class: " + getClass().getSimpleName();
        staticLog.info(String.format("%1$-" + logMessage.length() + "s", " ").replaceAll(" ", "*"));
        staticLog.info(logMessage);
        staticLog.info(String.format("%1$-" + logMessage.length() + "s", " ").replaceAll(" ", "*"));

        // clean common work dir
        beforeStartClean();

        copyTestResources();

        copyConfigFiles();

        // start nexus
        startNexus();

        // set security enabled/disabled as expected by current IT
        final boolean testRequiresSecurityEnabled =
            TestContainer.getInstance().getTestContext().isSecureTest()
                || Boolean.valueOf(System.getProperty("secure.test"));
        new UserMessageUtil(getXMLXStream(), MediaType.APPLICATION_XML)
            .makeAnonymousAdministrator(!testRequiresSecurityEnabled);

        // deploy artifacts
        deployArtifacts();

        runOnce();

        // TODO: we can remove this now that we have the soft restart
        NEEDS_INIT = false;
      }

      getEventInspectorsUtil().waitForCalmPeriod();
    }
  }

  @After
  public void afterTest() throws Exception {
    log.info("Cleanup");

    // reset this for each test
    TestContainer.getInstance().reset();
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @AfterClass
  public static void oncePerClassTearDown() throws Exception {
    staticLog.info("Static cleanup");

    try {
      TaskScheduleUtil.waitForAllTasksToStop();
      new EventInspectorsUtil().waitForCalmPeriod();
    }
    catch (IOException e) {
      // throw if server is already stopped, not a problem for me
    }

    // turn off security, of the current IT with security on won't affect the next IT
    TestContainer.getInstance().getTestContext().setSecureTest(false);

    // stop nexus
    stopNexus();

    profilerHelper.captureSnapshot();

    // stop per-IT plexus container
    TestContainer.getInstance().stopPlexusContainer();

    System.setProperties(systemPropertiesBackup);
  }

  protected void runOnce() throws Exception {
    // must override to happen something
  }

  protected File getNexusLogFile() {
    return new File(nexusLogDir + "/" + getTestId() + "/nexus.log");
  }

  protected void beforeStartClean() throws Exception {
    cleanWorkDir();
  }

  protected void copyTestResources() throws IOException {
    File source = new File(TestProperties.getString("test.resources.source.folder"), getTestResource(null));
    if (!source.exists()) {
      return;
    }

    File destination = new File(TestProperties.getString("test.resources.folder"), getTestResource(null));

    log.info("Copying test resources {} to {}", source, destination);

    FileTestingUtils.interpolationDirectoryCopy(source, destination, getTestProperties());
  }

  protected void copyConfigFiles() throws IOException {
    log.info("Copying config files");

    Map<String, String> testProperties = getTestProperties();

    copyConfigFile("nexus.xml", testProperties, WORK_CONF_DIR);

    // copy security config
    copyConfigFile("security.xml", testProperties, WORK_CONF_DIR);
    copyConfigFile("security-configuration.xml", testProperties, WORK_CONF_DIR);

    copyConfigFile("logback.properties", testProperties, WORK_CONF_DIR);
    copyConfigFile("logback-nexus.xml", testProperties, WORK_CONF_DIR);
  }

  protected static void cleanWorkDir() throws Exception {
    final File workDir = new File(AbstractNexusIntegrationTest.nexusWorkDir);

    staticLog.info("Cleaning work directory: {}", workDir);

    // to make sure I don't delete all my MP3's and pictures, or totally screw anyone.
    // check for 'target' and not allow any '..'
    if (workDir.getAbsolutePath().lastIndexOf("target") != -1
        && workDir.getAbsolutePath().lastIndexOf("..") == -1) {

      File[] filesToDelete = workDir.listFiles();
      if (filesToDelete != null) {
        for (File fileToDelete : filesToDelete) {
          // delete work dir
          if (fileToDelete != null) {
            FileUtils.forceDelete(fileToDelete);
          }
        }
      }
    }
  }

  /**
   * Deploys all the provided files needed before IT actually starts.
   */
  protected void deployArtifacts() throws Exception {
    File projectsDir = getTestResourceAsFile("projects");
    new DeployHelper().deployArtifacts(projectsDir);
  }

  @Deprecated
  protected void deployArtifacts(final File projectsDir) throws Exception {
    new DeployHelper().deployArtifacts(projectsDir);
  }

  @Deprecated
  protected void deployArtifacts(File project, String wagonHint, String deployUrl, Model model) throws Exception {
    new DeployHelper().deployArtifacts(project, wagonHint, deployUrl, model);
  }

  protected void startNexus() throws Exception {
    log.info("Starting Nexus");

    TestContainer.getInstance().getTestContext().useAdminForRequests();

    long mark = System.currentTimeMillis();
    try {
      getNexusStatusUtil().start(getTestId());

      // booting is now asynchronous, so have to wait for Nexus
      Thread.sleep(10000);
      for (int i = 0; i < 100; i++) {
        try {
          if (getNexusStatusUtil().isNexusRunning()) {
            return;
          }
        }
        catch (Exception ignore) {
          log.debug("Nexus is still booting, retrying...", ignore);
        }
        Thread.sleep(1000);
      }
    }
    catch (Exception e) {
      log.error(e.toString(), e);
      e.printStackTrace();
      throw e;
    }
    throw new RuntimeException("Nexus did not boot after " + (System.currentTimeMillis() - mark) / 1000 + "s!");
  }

  protected static void stopNexus() throws Exception {
    staticLog.info("Stopping Nexus");
    getNexusStatusUtil().stop();
  }

  protected void restartNexus() throws Exception {
    log.info("Restarting Nexus");
    stopNexus();
    startNexus();
  }

  protected File getOverridableFile(String file) {
    // the test can override the test config.
    File testConfigFile = getTestResourceAsFile("test-config/" + file);

    // if the tests doesn't have a different config then use the default.
    // we need to replace every time to make sure no one changes it.
    if (testConfigFile == null || !testConfigFile.exists()) {
      testConfigFile = getResource("default-configs/" + file);
    }
    else {
      log.debug("This test is using its own " + file + " " + testConfigFile);
    }
    return testConfigFile;
  }

  protected void copyConfigFile(String configFile, String destShortName, Map<String, String> variables, String path)
      throws IOException
  {
    // the test can override the test config.
    File testConfigFile = getOverridableFile(configFile);
    if (testConfigFile == null || !testConfigFile.exists()) {
      return;
    }

    File parent = new File(path);
    if (!parent.isAbsolute()) {
      parent = new File(nexusBaseDir, path == null ? RELATIVE_CONF_DIR : path);
    }

    File destFile = new File(parent, destShortName);
    log.info("Copying config file {} to: {}", configFile, destFile);

    FileTestingUtils.interpolationFileCopy(testConfigFile, destFile, variables);

  }

  // Overloaded helpers

  protected void copyConfigFile(String configFile, String path) throws IOException {
    copyConfigFile(configFile, new HashMap<String, String>(), path);
  }

  protected void copyConfigFile(String configFile, Map<String, String> variables, String path) throws IOException {
    copyConfigFile(configFile, configFile, variables, path);
  }

  /**
   * Returns a File if it exists, null otherwise. Files returned by this method must be located in the
   * "src/test/resourcs/nexusXXX/" folder.
   *
   * @param relativePath path relative to the nexusXXX directory.
   * @return A file specified by the relativePath. or null if it does not exist.
   */
  protected File getTestResourceAsFile(String relativePath) {
    return getResource(getTestResource(relativePath));
  }

  protected String getTestResource(String relativePath) {
    return getClass().getPackage().getName().replace(".", "/")
        + (relativePath == null || "/".equals(relativePath.trim()) ? "" : "/" + relativePath);
  }

  protected String getTestId() {
    String packageName = getClass().getPackage().getName();
    return packageName.substring(packageName.lastIndexOf('.') + 1, packageName.length());
  }

  /**
   * Returns a File if it exists, null otherwise. Files returned by this method must be located in the
   * "src/test/resourcs/nexusXXX/files/" folder.
   *
   * @param relativePath path relative to the files directory.
   * @return A file specified by the relativePath. or null if it does not exist.
   */
  protected File getTestFile(String relativePath) {
    return getTestResourceAsFile("files/" + relativePath);
  }

  public static File getResource(String resource) {
    staticLog.debug("Looking for resource: {}", resource);
    // URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );

    File rootDir = new File(TestProperties.getString("test.resources.folder"));
    File file = new File(rootDir, resource);

    //if (!file.exists()) {
    //  return null;
    //}
    return file;
  }

  @Deprecated
  protected File downloadSnapshotArtifact(String repository, Gav gav, File parentDir) throws IOException {
    return new DownloadHelper().downloadSnapshotArtifact(repository, gav, parentDir);
  }

  @Deprecated
  protected Metadata downloadMetadataFromRepository(Gav gav, String repoId) throws IOException, XmlPullParserException {
    return new DownloadHelper().downloadMetadataFromRepository(gav, repoId);
  }

  @Deprecated
  protected File downloadArtifact(String baseUrl, String groupId, String artifact, String version, String type, String classifier, String targetDirectory) throws IOException {
    return new DownloadHelper().downloadArtifact(baseUrl, groupId, artifact, version, type, classifier, targetDirectory);
  }

  @Deprecated
  protected File downloadFile(URL url, String targetFile) throws IOException {
    return RequestFacade.downloadFile(url, targetFile);
  }

  @Deprecated
  protected File downloadArtifact(String baseUrl, Gav gav, String targetDirectory) throws IOException {
    return new DownloadHelper().downloadArtifact(baseUrl, gav, targetDirectory);
  }

  @Deprecated
  protected File downloadArtifactFromRepository(String repoId, Gav gav, String targetDirectory) throws IOException {
    return new DownloadHelper().downloadArtifactFromRepository(repoId, gav, targetDirectory);
  }

  @Deprecated
  protected File downloadArtifactFromGroup(String groupId, Gav gav, String targetDirectory) throws IOException {
    return new DownloadHelper().downloadArtifactFromGroup(groupId, gav, targetDirectory);
  }

  protected boolean deleteFromRepository(String repository, String groupOrArtifactPath) throws IOException {
    String serviceURI = String.format("service/local/repositories/%s/content/%s", repository, groupOrArtifactPath);

    Status status = RequestFacade.doGetForStatus(serviceURI);
    if (status.equals(Status.CLIENT_ERROR_NOT_FOUND)) {
      log.debug("Not deleted because it didn't exist: {}", serviceURI);
      return true;
    }

    log.debug("deleting: {}", serviceURI);
    status = RequestFacade.doDeleteForStatus(serviceURI, null);
    boolean deleted = status.isSuccess();

    if (!deleted) {
      log.debug("Failed to delete: {} - Status: {}", serviceURI, status);
    }

    // fake it because the artifact doesn't exist
    // TODO: clean this up.
    if (status.getCode() == 404) {
      deleted = true;
    }

    return deleted;
  }

  protected String getBaseNexusUrl() {
    return nexusBaseUrl;
  }

  protected String getNexusTestRepoUrl(String repo) {
    return String.format("%s%s%s/", nexusBaseUrl, REPOSITORY_RELATIVE_URL, repo);
  }

  protected String getNexusTestRepoUrl() {
    return getNexusTestRepoUrl(getTestRepositoryId());
  }

  protected String getNexusTestRepoServiceUrl() {
    return String.format("%sservice/local/repositories/%s/content/", nexusBaseUrl, getTestRepositoryId());
  }

  protected String getTestRepositoryId() {
    return testRepositoryId;
  }

  protected void setTestRepositoryId(String repoId) {
    testRepositoryId = repoId;
  }

  protected String getRepositoryUrl(String repoId) {
    return String.format("%s%s%s/", nexusBaseUrl, REPOSITORY_RELATIVE_URL, repoId);
  }

  protected String getGroupUrl(String groupId) {
    return String.format("%s%s%s/", nexusBaseUrl, GROUP_REPOSITORY_RELATIVE_URL, groupId);
  }

  protected boolean printKnownErrorButDoNotFail(Class<? extends AbstractNexusIntegrationTest> clazz, String... tests) {
    StringBuilder error = new StringBuilder();
    error.append("*********************************************************************************");
    error.append("\n* This test is being skipped because its known to fail,");
    error.append("\n* It is a very minor error, and is only a problem if you start sending in ");
    error.append("\n* raw REST request to Nexus. (it is not a security problem)");
    error.append("*\n*\n");
    error.append("*\n* TestClass: ").append(clazz);
    for (String test : tests) {
      error.append("*\n* Test: ").append(test);
    }
    error.append("\n**********************************************************************************");

    log.info(error.toString());
    return true;
  }

  protected XStream getXMLXStream() {
    return XStreamFactory.getXmlXStream();
  }

  protected XStream getJsonXStream() {
    return XStreamFactory.getJsonXStream();
  }

  protected Map<String, String> getTestProperties() {
    HashMap<String, String> variables = new HashMap<String, String>();
    variables.putAll(TestProperties.getAll());
    variables.put("test-id", getTestId());
    return variables;
  }
}
