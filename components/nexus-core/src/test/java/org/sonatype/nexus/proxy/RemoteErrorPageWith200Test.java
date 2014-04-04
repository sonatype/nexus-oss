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

package org.sonatype.nexus.proxy;

import java.io.InputStream;

import org.sonatype.nexus.proxy.internal.ErrorServlet;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.httpclient.HttpClientRemoteStorage;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.DeliverBodyBehaviour;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class RemoteErrorPageWith200Test
    extends AbstractProxyTestEnvironment
{
  private RemoteRepositoryStorage remoteStorage;

  private ProxyRepository aProxyRepository;

  @Rule
  public ServerResource server = new ServerResource(Server.server().serve("/*")
      .withServlet(new ErrorServlet())
      .getServerProvider());

  @Override
  public void setUp()
      throws Exception
  {
    super.setUp();
    this.remoteStorage =
        this.lookup(RemoteRepositoryStorage.class, HttpClientRemoteStorage.PROVIDER_STRING);
    aProxyRepository =
        lookup(RepositoryRegistry.class).getRepositoryWithFacet("200ErrorTest", ProxyRepository.class);
  }

  @Override
  protected EnvironmentBuilder getEnvironmentBuilder()
      throws Exception
  {
    return new M2TestsuiteEnvironmentBuilder("200ErrorTest")
    {
      protected void createRemoteServer(final String repoId) {
        // nop, we will create server using a rule
      }
    };
  }

  @Test
  public void testRemoteReturnsErrorWith200StatusHeadersNotSet()
      throws Exception
  {

    String expectedContent = "my cool expected content";
    ErrorServlet.CONTENT = expectedContent;
    ErrorServlet.clearHeaders();

    // remote request
    ResourceStoreRequest storeRequest = new ResourceStoreRequest("random/file.txt");
    DefaultStorageFileItem item =
        (DefaultStorageFileItem) remoteStorage.retrieveItem(aProxyRepository, storeRequest, server.getServerProvider().getUrl().toExternalForm());

    // result should be HTML
    try (InputStream io = item.getInputStream()) {
      String content = IOUtils.toString(io);
      Assert.assertEquals(expectedContent, content);
    }
  }

  @Test
  public void testRemoteReturnsErrorWith200StatusHeadersSet()
      throws Exception
  {

    String expectedContent = "error page";
    ErrorServlet.CONTENT = expectedContent;
    ErrorServlet.addHeader(HttpClientRemoteStorage.NEXUS_MISSING_ARTIFACT_HEADER, "true");

    // remote request
    ResourceStoreRequest storeRequest = new ResourceStoreRequest("random/file.txt");
    try {
      DefaultStorageFileItem item =
          (DefaultStorageFileItem) remoteStorage.retrieveItem(aProxyRepository, storeRequest, server.getServerProvider().getUrl().toExternalForm());
      Assert.fail("expected  RemoteStorageException");
    }
    // expect artifact not found
    catch (RemoteStorageException e) {
      // expected
    }
  }

}
