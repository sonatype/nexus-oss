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

package org.sonatype.nexus.testsuite.misc.nexus3860;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.NexusWebappLayout;
import org.sonatype.nexus.test.utils.ResponseMatchers;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.nexus.testsuite.plugin.nexus2810.PluginConsoleMessageUtil;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractCargoIT
    extends TestSupport
{
  private InstalledLocalContainer container;

  public abstract String getContainer();

  public abstract File getContainerLocation();

  public File getWarFile() {
    return util.resolveFile("target/nexus/webapp");
  }

  @BeforeClass
  public static void startContainer() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
    TestContainer.getInstance().getTestContext().useAdminForRequests();
  }

  @Before
  public void setUp()
      throws Exception
  {
    if (container == null) {
      fixPlexusProperties();
      changeStartupLoggingToDebug();

      WAR war = new WAR(getWarFile().getAbsolutePath());
      war.setContext("nexus");

      File configHome = new File("target/conatiner-configs", getContainer()).getAbsoluteFile();

      ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
      LocalConfiguration configuration =
          (LocalConfiguration) configurationFactory.createConfiguration(getContainer(), ContainerType.INSTALLED,
              ConfigurationType.STANDALONE, configHome.getAbsolutePath());
      configuration.addDeployable(war);
      configuration.setProperty(ServletPropertySet.PORT, TestProperties.getString("nexus.application.port"));
      container =
          (InstalledLocalContainer) new DefaultContainerFactory().createContainer(getContainer(),
              ContainerType.INSTALLED, configuration);
      container.setHome(getContainerLocation().getAbsolutePath());

      // Shut down WL feature, as these ITs are bouncing Nexus very frequently, while
      // WL does discovery in background. For some reason, codehaus prefix file
      // fetch on bamboo took 20s, so while WL shutdown is not fixed, disabling
      // WL for now here.

      final HashMap<String, String> sysProps = new HashMap<String, String>();
      sysProps.put(ConfigImpl.FEATURE_ACTIVE_KEY, Boolean.FALSE.toString());
      container.setSystemProperties(sysProps);

      container.setTimeout(5 * 60 * 1000);// 5 minutes
      container.start();
    }
  }

  /**
   * To be sure we get as much logging as possible to scan for errors loading plugins or other hidden goop, set
   * startup logging to DEBUG level
   */
  private void changeStartupLoggingToDebug()
      throws Exception
  {
    // get the default logback.properties that will be deployed on 1st startup
    File startupLogbackPropsFile = new File(getITNexusWorkDirPath() + "/conf/logback.properties");
    try {
      URL configUrl = this.getClass().getResource("/META-INF/log/logback.properties");
      FileUtils.copyURLToFile(configUrl, startupLogbackPropsFile);
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not create logback.properties as "
          + startupLogbackPropsFile.getAbsolutePath());
    }

    // now load the prop file and change the root.level to DEBUG
    Properties p = new Properties();
    FileReader r = new FileReader(startupLogbackPropsFile);
    try {
      p.load(r);
    }
    finally {
      r.close();
    }
    assertThat((String) p.get("root.level"), is("INFO")); // sanity

    // reset it
    p.setProperty("root.level", "DEBUG");

    Writer w = new FileWriter(startupLogbackPropsFile);
    try {
      p.store(w, "Startup properties that override the default root.level to DEBUG - created by "
          + getClass().getName());
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load logback.properties from "
          + startupLogbackPropsFile.getAbsolutePath());
    }
    finally {

      w.close();
    }
  }

  /**
   * Customize the work dir per IT by mucking with the nexus-work-dir property
   */
  private void fixPlexusProperties()
      throws Exception
  {
    File plexusProps = new File(getWarFile(), NexusWebappLayout.PATH_PLEXUS_PROPERTIES);
    Properties p = new Properties();
    FileReader r = new FileReader(plexusProps);
    try {
      p.load(r);
    }
    finally {
      r.close();
    }
    p.setProperty("nexus-work", getITNexusWorkDirPath());
    Writer w = new FileWriter(plexusProps);
    try {
      p.store(w, null);
    }
    finally {
      w.close();
    }
  }

  protected String getITNexusWorkDirPath() {
    return TestProperties.getString("nexus-work-dir") + "-" + getClass().getSimpleName();
  }

  @After
  public void stopContainer() {
    if (container != null) {
      try {
        container.stop();
      }
      catch (org.codehaus.cargo.container.ContainerException e) {
        // ignore it
      }
    }
  }

  @Test
  public void checkStatus()
      throws Exception
  {
    // this line produces log entries that makes it look like bundle too is started but is actually not
    assertEquals(
        "STARTED",
        new NexusStatusUtil(AbstractNexusIntegrationTest.nexusApplicationPort).getNexusStatus().getData().getState());
  }

  protected PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

  @Test
  public void checkPlugins()
      throws Exception
  {
    checkStatus();
    checkLogs();

    TestContainer.getInstance().getTestContext().useAdminForRequests();

    List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

    assertNotNull(pluginInfos);
    assertFalse(pluginInfos.isEmpty());

    for (PluginInfoDTO info : pluginInfos) {
      assertEquals("ACTIVATED", info.getStatus());
    }
  }

  @Test
  public void checkLogs()
      throws Exception
  {
    checkStatus();

    final Response response = RequestFacade.sendMessage("service/local/logs", Method.GET);
    try {
      String responseText = response.getEntity().getText();

      Assert.assertEquals("Status: \n" + responseText, response.getStatus().getCode(), 200);

      LogsListResourceResponse logListResponse =
          (LogsListResourceResponse) XStreamFactory.getXmlXStream().fromXML(responseText);
      List<LogsListResource> logList = logListResponse.getData();
      Assert.assertTrue("Log List should contain at least 1 log.", logList.size() > 0);

      for (Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); ) {
        LogsListResource logResource = iter.next();

        this.downloadAndConfirmLog(logResource.getResourceURI(), logResource.getName());
      }
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  /**
   * Verify the logging output does not contain any nasty errors and such
   */
  private void downloadAndConfirmLog(String logURI, String name)
      throws Exception
  {
    Response response = null;
    InputStreamReader reader = null;
    BufferedReader bReader = null;
    try {
      response = RequestFacade.sendMessage(new URL(logURI), Method.GET, null);
      assertThat(response, ResponseMatchers.isSuccessful());
      InputStream stream = response.getEntity().getStream();
      if (stream == null) {
        Assert.fail("Stream was null: " + response.getEntity().getText());
      }

      // get the first 10000 chars from the downloaded log
      reader = new InputStreamReader(stream);
      bReader = new BufferedReader(reader);

      StringBuilder downloadedLog = new StringBuilder();

      int lineCount = 10000;
      while (bReader.ready() && lineCount-- > 0) {
        downloadedLog.append((char) bReader.read());
      }

      String downloadedLogStr = downloadedLog.toString();

      assertThat("Should have been DEBUG level logging so there should have been DEBUG in log",
          downloadedLogStr, containsString("DEBUG"));

      List<String> falsePositives = Lists.newArrayList();
      falsePositives.add("org/sonatype/nexus/rest/error/reporting/ErrorReportingPlexusResource");
      falsePositives.add("org.sonatype.nexus.rest.error.reporting.ErrorReportingPlexusResource");
      falsePositives.add("org.sonatype.nexus.error.reporting.DefaultErrorReportingManager");

      for (String fp : falsePositives) {
        downloadedLogStr = downloadedLogStr.replace(fp, "");
      }

      assertThat(downloadedLogStr, not(containsString("error")));
      assertThat(downloadedLogStr, not(containsString("exception")));

    }
    finally {
      RequestFacade.releaseResponse(response);
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(bReader);
    }
  }

}
