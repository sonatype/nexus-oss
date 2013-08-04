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

package org.sonatype.nexus.proxy.maven.routing.internal.scrape;

import java.net.SocketException;

import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.routing.internal.scrape.Page.UnexpectedPageResponse;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

public class PageTest
    extends TestSupport
{
  @Mock
  private MavenProxyRepository mavenProxyRepository;

  @Test
  public void simpleCase()
      throws Exception
  {
    final Server server =
        Server.withPort(0).serve("/foo/bar/").withBehaviours(Behaviours.content("<html></html>"));
    server.start();
    try {
      final String repoRootUrl = server.getUrl().toString() + "/foo/bar/";
      when(mavenProxyRepository.getRemoteUrl()).thenReturn(repoRootUrl);
      final ScrapeContext context = new ScrapeContext(mavenProxyRepository, new DefaultHttpClient(), 2);
      final Page page = Page.getPageFor(context, repoRootUrl);
      assertThat(page.getUrl(), equalTo(repoRootUrl));
      assertThat(page.getHttpResponse().getStatusLine().getStatusCode(), equalTo(200));
      assertThat(page.getDocument(), notNullValue());
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void error404WithBody()
      throws Exception
  {
    final Server server = Server.withPort(0).serve("/foo/bar/").withBehaviours(Behaviours.error(404));
    server.start();
    try {
      final String repoRootUrl = server.getUrl().toString() + "/foo/bar/";
      when(mavenProxyRepository.getRemoteUrl()).thenReturn(repoRootUrl);
      final ScrapeContext context = new ScrapeContext(mavenProxyRepository, new DefaultHttpClient(), 2);
      final Page page = Page.getPageFor(context, repoRootUrl);
      assertThat(page.getUrl(), equalTo(repoRootUrl));
      assertThat(page.getHttpResponse().getStatusLine().getStatusCode(), equalTo(404));
      assertThat(page.getDocument(), notNullValue());
    }
    finally {
      server.stop();
    }
  }

  @Test(expected = UnexpectedPageResponse.class)
  public void error500IsException()
      throws Exception
  {
    final Server server = Server.withPort(0).serve("/*").withBehaviours(Behaviours.error(500));
    server.start();
    try {
      final String repoRootUrl = server.getUrl().toString() + "/foo/bar/";
      when(mavenProxyRepository.getRemoteUrl()).thenReturn(repoRootUrl);
      final ScrapeContext context = new ScrapeContext(mavenProxyRepository, new DefaultHttpClient(), 2);
      final Page page = Page.getPageFor(context, repoRootUrl);
    }
    finally {
      server.stop();
    }
  }

  @Test(expected = SocketException.class)
  public void errorConnectionRefusedException()
      throws Exception
  {
    final String repoRootUrl;
    final Server server = Server.withPort(0).serve("/*").withBehaviours(Behaviours.error(500));
    server.start();
    try {
      repoRootUrl = server.getUrl().toString() + "/foo/bar/";
    }
    finally {
      server.stop();
    }
    when(mavenProxyRepository.getRemoteUrl()).thenReturn(repoRootUrl);
    final ScrapeContext context = new ScrapeContext(mavenProxyRepository, new DefaultHttpClient(), 2);
    final Page page = Page.getPageFor(context, repoRootUrl);
  }
}
