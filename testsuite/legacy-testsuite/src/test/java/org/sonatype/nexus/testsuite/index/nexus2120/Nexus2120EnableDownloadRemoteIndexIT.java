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

package org.sonatype.nexus.testsuite.index.nexus2120;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.index.tasks.UpdateIndexTask;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeNodeDTO;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.restlet.data.MediaType;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class Nexus2120EnableDownloadRemoteIndexIT
    extends AbstractNexusIntegrationTest
{

  final String URI = "service/local/repositories/basic/index_content/";

  protected static final int webProxyPort;

  static {
    webProxyPort = TestProperties.getInteger("webproxy-server-port");
  }

  @Rule
  public ServerResource serverResource = new ServerResource(buildServerProvider());

  private final List<String> accessedPaths = Lists.newArrayList();

  protected ServerProvider buildServerProvider() {
    final ServerProvider serverProvider = Server.withPort(TestProperties.getInteger("webproxy-server-port"))
        .serve("/repository/*").fromDirectory(getTestFile("basic"))
        .getServerProvider();
    serverProvider.addFilter("/repository/*", new Filter()
    {
      @Override
      public void init(final FilterConfig filterConfig) throws ServletException {
        // nop
      }

      @Override
      public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
          throws IOException, ServletException
      {
        accessedPaths.add(((HttpServletRequest) request).getPathInfo());
        chain.doFilter(request, response);
      }

      @Override
      public void destroy() {
        // nop
      }
    });
    return serverProvider;
  }

  private RepositoryMessageUtil repoUtil;

  @Before
  public void start()
      throws Exception
  {
    repoUtil = new RepositoryMessageUtil(this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML);
  }

  @After
  public void stop()
      throws Exception
  {
    repoUtil = null;
  }

  @Test
  public void downloadChecksumTest()
      throws Exception
  {
    RepositoryResource basic = (RepositoryResource) repoUtil.getRepository("basic");
    // ensure URL
    assertThat(basic.getRemoteStorage().getRemoteStorageUrl(),
        equalTo("http://localhost:" + webProxyPort + "/repository/"));
    // ensure is not downloading index
    assertThat(basic.isDownloadRemoteIndexes(), is(false));

    // reindex once
    RepositoryMessageUtil.updateIndexes("basic");
    TaskScheduleUtil.waitForAllTasksToStop(UpdateIndexTask.class);

    // first try, download remote index set to false, no index should be pulled
    assertThat(accessedPaths, empty());

    // server changed here, a 404 is no longer returned if index_context is empty, 404 will only be returned
    // if index_context does not exist (or repo does not exist)
    String content = RequestFacade.doGetForText(URI);

    XStream xstream = XStreamFactory.getXmlXStream();

    xstream.processAnnotations(IndexBrowserTreeNodeDTO.class);
    xstream.processAnnotations(IndexBrowserTreeViewResponseDTO.class);

    XStreamRepresentation re = new XStreamRepresentation(xstream, content, MediaType.APPLICATION_XML);
    IndexBrowserTreeViewResponseDTO resourceResponse =
        (IndexBrowserTreeViewResponseDTO) re.getPayload(new IndexBrowserTreeViewResponseDTO());

    assertThat("without index downloaded root node does not have children", resourceResponse.getData().getChildren(),
        is(nullValue()));

    // I changed my mind, I do wanna remote index
    basic.setDownloadRemoteIndexes(true);
    repoUtil.updateRepo(basic);

    // reindex again
    RepositoryMessageUtil.updateIndexes("basic");
    TaskScheduleUtil.waitForAllTasksToStop(UpdateIndexTask.class);

    // did nexus downloaded indexes?
    assertThat("Bad: " + accessedPaths, accessedPaths, hasItem("/.index/nexus-maven-repository-index.gz"));

    RequestFacade.doGet(URI);
  }
}
