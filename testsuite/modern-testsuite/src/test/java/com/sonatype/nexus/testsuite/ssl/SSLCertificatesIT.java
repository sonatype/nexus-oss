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
package com.sonatype.nexus.testsuite.ssl;

import javax.inject.Inject;

import com.sonatype.nexus.ssl.client.Certificate;

import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.sisu.bl.support.port.PortReservationService;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sun.jersey.api.client.ClientResponse.Status;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.nexus.testsuite.support.hamcrest.UniformInterfaceExceptionMatchers.exceptionWithStatus;

/**
 * ITs related to retrieving certificates.
 *
 * @since 1.0
 */
public class SSLCertificatesIT
    extends SSLITSupport
{

  private Server httpServer;

  private GreenMail mailServer;

  @Inject
  private PortReservationService portReservationService;

  public SSLCertificatesIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @After
  public void stopHttpServer()
      throws Exception
  {
    if (httpServer != null) {
      httpServer.stop();
    }
  }

  @After
  public void stopMailServer()
      throws Exception
  {
    if (mailServer != null) {
      mailServer.stop();
    }
  }

  /**
   * Verify that we can retrieve certificate from host:port.
   */
  @Test
  public void host()
      throws Exception
  {
    httpServer = Server.withPort(0).withKeystore("keystore", "password").
        serve("/*").withBehaviours(
        Behaviours.get(testData().resolveFile("proxy-repo"))
    ).start();

    final Certificate certificate = certificates().get("localhost", httpServer.getPort());

    assertThat(certificate, is(notNullValue()));
    assertThat(certificate.pem(), is(notNullValue()));
    assertThat(certificate.fingerprint(), is("CB:11:54:75:02:87:33:42:54:33:7D:2E:10:48:D7:4E:AE:BB:5C:90"));
  }

  /**
   * Verify that in case of retrieving certificate from an non https host, we get a specific exception.
   */
  @Test
  public void retrieveFromPlainHttp()
      throws Exception
  {
    httpServer = Server.withPort(0).
        serve("/*").withBehaviours(Behaviours.get(testData().resolveFile("proxy-repo"))
    ).start();

    thrown.expect(exceptionWithStatus(Status.NOT_FOUND));
    //thrown.expectMessage("SSL");

    certificates().get("localhost", httpServer.getPort());
  }

  /**
   * Verify that in case of retrieving certificate from an unknown host, we get a specific exception.
   */
  @Test
  public void retrieveFromUnknownHost()
      throws Exception
  {
    thrown.expect(exceptionWithStatus(Status.NOT_FOUND));
    //thrown.expectMessage("Unknown host 'foo'");

    certificates().get("foo", 443);
  }

  /**
   * Verify that we can retrieve certificate from a repository with a self signed certificate.
   */
  @Test
  public void selfSignedCertificate()
      throws Exception
  {
    httpServer = Server.withPort(0).withKeystore("keystore", "password").
        serve("/*").withBehaviours(
        Behaviours.get(testData().resolveFile("proxy-repo"))
    ).start();

    final MavenProxyRepository proxyRepository =
        repositories().create(MavenProxyRepository.class, repositoryIdForTest())
            .asProxyOf(httpServer.getUrl().toExternalForm())
            .doNotDownloadRemoteIndexes()
            .save();

    final Certificate certificate = certificates().get(proxyRepository.id());

    assertThat(certificate, is(notNullValue()));
    assertThat(certificate.pem(), is(notNullValue()));
    assertThat(certificate.fingerprint(), is("CB:11:54:75:02:87:33:42:54:33:7D:2E:10:48:D7:4E:AE:BB:5C:90"));
  }

  /**
   * Verify that we can retrieve certificate from a repository with a CA signed certificate.
   */
  @Test
  public void caSignedCertificate()
      throws Exception
  {
    final MavenProxyRepository proxyRepository =
        repositories().create(MavenProxyRepository.class, repositoryIdForTest())
            .asProxyOf("https://repository.sonatype.org/content/groups/sonatype-public-grid/")
            .doNotDownloadRemoteIndexes()
            .save();

    final Certificate certificate = certificates().get(proxyRepository.id());

    assertThat(certificate, is(notNullValue()));
    assertThat(certificate.pem(), is(notNullValue()));
    assertThat(certificate.fingerprint(), is("61:96:33:FA:AF:52:1C:EC:D5:97:CF:CC:C3:CE:15:20:F9:CC:22:6B"));
  }

  /**
   * Verify certificate details got from a certificate in PEM format.
   */
  @Test
  public void detailsFromPem()
      throws Exception
  {
    final Certificate certificate = certificates().getDetails(
        FileUtils.readFileToString(testData().resolveFile("pem.txt"))
    );
    assertThat(certificate, is(notNullValue()));
    assertThat(certificate.pem(), is(notNullValue()));
    assertThat(certificate.fingerprint(), is("19:08:03:84:0E:1D:0E:46:05:D4:17:50:5D:A1:FA:34:36:6E:06:C1"));
  }

  /**
   * Verify that we can retrieve certificate from a mail server host:port.
   */
  @Test
  public void retrieveFromMailServer()
      throws Exception
  {
    mailServer = new GreenMail(
        new ServerSetup(portReservationService.reservePort(), null, ServerSetup.PROTOCOL_SMTPS)
    );
    mailServer.start();

    // sleep for 10 seconds to give time to mail server to startup
    logger.info("Waiting for mail server for 10s");
    Thread.sleep(10000);

    final Certificate certificate = certificates().get(
        "localhost", mailServer.getSmtps().getPort(), ServerSetup.PROTOCOL_SMTPS
    );

    assertThat(certificate, is(notNullValue()));
    assertThat(certificate.pem(), is(notNullValue()));
    assertThat(certificate.fingerprint(), is("F2:E7:5A:B6:BC:46:17:AB:54:8E:6E:35:23:62:6E:38:A5:28:62:D6"));
  }

}
