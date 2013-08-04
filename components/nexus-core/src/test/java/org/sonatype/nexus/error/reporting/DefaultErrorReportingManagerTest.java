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

package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipFile;

import org.sonatype.jira.AttachmentHandler;
import org.sonatype.jira.mock.MockAttachmentHandler;
import org.sonatype.jira.mock.StubJira;
import org.sonatype.jira.test.JiraXmlRpcTestServlet;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

import com.google.common.io.Files;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.swizzle.jira.authentication.AuthenticationSource;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.swizzle.jira.Issue;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.containsEntry;

public class DefaultErrorReportingManagerTest
    extends NexusAppTestSupport
{
  private DefaultErrorReportingManager manager;

  private NexusConfiguration nexusConfig;

  private File unzipHomeDir = null;

  private JettyServerProvider provider;

  private MockAttachmentHandler handler;

  @Override
  protected void setUp()
      throws Exception
  {
    setupJiraMock(util.resolvePath("src/test/resources/jira-mock.db"));

    super.setUp();

    unzipHomeDir = new File(getPlexusHomeDir(), "unzip");
    unzipHomeDir.mkdirs();

    nexusConfig = lookup(NexusConfiguration.class);

    manager = (DefaultErrorReportingManager) lookup(ErrorReportingManager.class);

    enableErrorReports(false);
  }

  private void setupJiraMock(String dbPath)
      throws Exception
  {
    StubJira mock = new StubJira();
    FileInputStream in = null;
    try {
      in = new FileInputStream(dbPath);
      mock.setDatabase(IOUtil.toString(in));

      handler = new MockAttachmentHandler();
      handler.setMock(mock);
      List<AttachmentHandler> handlers = Arrays.<AttachmentHandler>asList(handler);
      provider = new JettyServerProvider();
      provider.addServlet(new JiraXmlRpcTestServlet(mock, provider.getUrl(), handlers));
      provider.start();
    }
    finally {
      IOUtil.close(in);
    }
  }

  @Override
  protected void customizeContext(final Context ctx) {
    try {
      ctx.put("pr.serverUrl", provider.getUrl().toString());
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      ctx.put("pr.serverUrl", "https://issues.sonatype.org");
    }
    ctx.put("pr.auth.login", "sonatype_problem_reporting");
    ctx.put("pr.auth.password", "____");
    ctx.put("pr.project", "SBOX");
    ctx.put("pr.component", "Nexus");
    ctx.put("pr.issuetype.default", "1");
    super.customizeContext(ctx);
  }

  @Override
  protected void customizeContainerConfiguration(final ContainerConfiguration configuration) {
    super.customizeContainerConfiguration(configuration);
    configuration.setClassPathScanning("ON");
    configuration.setAutoWiring(true);
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    super.tearDown();

    cleanDir(unzipHomeDir);
    provider.stop();
  }

  private void enableErrorReports(boolean useProxy)
      throws Exception
  {
    manager.setEnabled(true);
    try {
      manager.setJIRAUrl(provider.getUrl().toString());
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      manager.setJIRAUrl("https://issues.sonatype.org");
    }
    manager.setJIRAProject("SBOX");
    manager.setJIRAUsername("jira");
    manager.setJIRAPassword("jira");

    nexusConfig.saveConfiguration();
  }

  @Test
  public void testJiraAccess()
      throws Exception
  {
    ErrorReportRequest request = new ErrorReportRequest();

    try {
      throw new Exception("Test exception " + Long.toHexString(System.currentTimeMillis()));
    }
    catch (Exception e) {
      request.setThrowable(e);
    }

    // First make sure item doesn't already exist
    List<Issue> issues =
        manager.retrieveIssues("APR: " + request.getThrowable().getMessage(), getAuth());

    assertThat(issues, hasSize(0));

    manager.handleError(request);

    issues =
        manager.retrieveIssues("APR: " + request.getThrowable().getMessage(), getAuth());

    Assert.assertEquals(1, issues.size());

    manager.handleError(request);

    issues =
        manager.retrieveIssues("APR: " + request.getThrowable().getMessage(), getAuth());

    Assert.assertEquals(1, issues.size());
  }

  @Test
  public void testPackageFiles()
      throws Exception
  {
    addBackupFiles(getConfHomeDir());
    addDirectory("test-directory", new String[]{"filename1.file", "filename2.file", "filename3.file"});
    addDirectory("nested-test-directory/more-nested-test-directory", new String[]{
        "filename1.file",
        "filename2.file", "filename3.file"
    });

    Exception exception;

    try {
      throw new Exception("Test exception");
    }
    catch (Exception e) {
      exception = e;
    }

    manager.setEnabled(true);

    nexusConfiguration.saveConfiguration();

    ErrorReportRequest request = new ErrorReportRequest();
    request.setThrowable(exception);

    // submit request to mock to trigger bundle creation
    final ErrorReportResponse response = manager.handleError(request);
    final String url = response.getJiraUrl();

    // specific to mock jira
    final String key = url.substring(url.indexOf("SBOX"));

    final Matcher<ZipFile> containsDefaultEntries = allOf(
        containsEntry("nexus.xml"),
        // containsEntry( "security.xml" ), // loaded too early for test setup, omitted b/c model is null
        containsEntry("security-configuration.xml"),
        containsEntry("exception.txt"),
        containsEntry("contextListing.txt")
    );

    final Matcher<ZipFile> containsAdditionalEntries = allOf(
        containsEntry("conf/test-directory/filename1.file"),
        containsEntry("conf/test-directory/filename2.file"),
        containsEntry("conf/test-directory/filename3.file"),
        containsEntry("conf/nested-test-directory/more-nested-test-directory/filename1.file"),
        containsEntry("conf/nested-test-directory/more-nested-test-directory/filename2.file"),
        containsEntry("conf/nested-test-directory/more-nested-test-directory/filename3.file")
    );

    final File zipDir = nexusConfig.getWorkingDirectory(DefaultErrorReportingManager.ERROR_REPORT_DIR);
    final ZipFile zipFile = new ZipFile(zipDir.listFiles()[0]);
    try {
      assertThat(zipFile, containsDefaultEntries);
      assertThat(zipFile, containsAdditionalEntries);
    }
    finally {
      zipFile.close();
    }
  }

  private void addBackupFiles(File dir)
      throws Exception
  {
    new File(dir, "nexus.xml.bak").createNewFile();
    new File(dir, "security.xml.bak").createNewFile();
  }

  private void addDirectory(String path, String[] filenames)
      throws Exception
  {
    File confDir = new File(getConfHomeDir(), path);
    confDir.mkdirs();

    for (String filename : filenames) {
      Files.write("test".getBytes(), new File(confDir, filename));
    }
  }

  @Test
  public void testTaskFailure()
      throws Exception
  {
    // since Timeline moved into plugin, we need EventInspectorHost too
    // That's why we add a "ping" for Nexus component, and it installs the EventInspectorHost too
    // awake nexus, to awake EventInspector host too
    lookup(Nexus.class);
    // we will need this to properly wait the async event inspectors to finish
    final EventInspectorHost eventInspectorHost = lookup(EventInspectorHost.class);

    String msg = "Runtime exception " + Long.toHexString(System.currentTimeMillis());
    ExceptionTask task = (ExceptionTask) lookup(SchedulerTask.class, "ExceptionTask");
    task.setMessage(msg);

    // First make sure item doesn't already exist
    Collection<?> issues =
        manager.retrieveIssues("APR: " + new RuntimeException(msg).getMessage(), getAuth());

    // empty() has weirdo generics
    assertThat(issues, hasSize(0));

    doCall(task, eventInspectorHost);

    issues =
        manager.retrieveIssues("APR: " + new RuntimeException(msg).getMessage(), getAuth());

    assertThat(issues, hasSize(1));

    doCall(task, eventInspectorHost);

    issues =
        manager.retrieveIssues("APR: " + new RuntimeException(msg).getMessage(), getAuth());

    assertThat(issues, hasSize(1));
  }

  private void doCall(final NexusTask<?> task, final EventInspectorHost inspectorHost)
      throws InterruptedException
  {
    try {
      task.call();
    }
    catch (Throwable t) {
    }
    finally {
      do {
        Thread.sleep(100);
      }
      while (!inspectorHost.isCalmPeriod());
    }
  }

  public AuthenticationSource getAuth() {
    return new DefaultAuthenticationSource(manager.getValidJIRAUsername(), manager.getValidJIRAPassword());
  }
}
