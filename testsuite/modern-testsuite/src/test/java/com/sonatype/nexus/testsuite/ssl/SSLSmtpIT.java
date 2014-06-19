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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;
import org.sonatype.nexus.client.core.subsystem.Restlet1xClient;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.rest.model.SmtpSettingsResourceRequest;
import org.sonatype.sisu.bl.support.port.PortReservationService;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.Test;

import static com.sonatype.nexus.ssl.model.SMTPTrustStoreKey.smtpTrustStoreKey;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * ITs related to SMTP keys / access.
 *
 * @since 1.0
 */
public class SSLSmtpIT
    extends SSLITSupport
{

  @Inject
  private PortReservationService portReservationService;

  public SSLSmtpIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    return super.configureNexus(configuration)
        .setSystemProperty(
            "org.sonatype.nexus.ssl.smtp.checkServerIdentity", "false"
        );
  }

  /**
   * Verify SMTP trust store key.
   */
  @Test
  public void manageSMTPTrustStoreKey()
      throws Exception
  {
    assertThat(truststore().isEnabledFor(smtpTrustStoreKey()), is(false));

    truststore().enableFor(smtpTrustStoreKey());
    assertThat(truststore().isEnabledFor(smtpTrustStoreKey()), is(true));

    truststore().disableFor(smtpTrustStoreKey());
    assertThat(truststore().isEnabledFor(smtpTrustStoreKey()), is(false));
  }

  @Test
  public void sendEmailToSelfSignedEmailServer()
      throws Exception
  {
    truststore().enableFor(smtpTrustStoreKey());
    assertThat(truststore().isEnabledFor(smtpTrustStoreKey()), is(true));

    GreenMail mailServer = null;
    try {
      mailServer = new GreenMail(
          new ServerSetup(portReservationService.reservePort(), null, ServerSetup.PROTOCOL_SMTPS)
      );
      mailServer.start();
      final int mailPort = mailServer.getSmtps().getPort();

      // it should fail as mail server (self signed) certificate is not trusted
      try {
        sendTestEmail(mailPort);
        assertThat("Expected to fail with Exception", false);
      }
      catch (NexusClientErrorResponseException e) {
        assertThat(e.getResponseBody(), containsString("Failed to send validation e-mail"));
      }

      // trust mail server (self signed) certificate
      certificates().get("localhost", mailPort, ServerSetup.PROTOCOL_SMTPS).save();

      sendTestEmail(mailPort);
    }
    finally {
      if (mailServer != null) {
        portReservationService.cancelPort(mailServer.getSmtps().getPort());
        mailServer.stop();
      }
    }
  }

  private void sendTestEmail(final int port) {
    final SmtpSettingsResource smtpSettings = new SmtpSettingsResource();
    smtpSettings.setHost("localhost");
    smtpSettings.setPort(port);
    smtpSettings.setTlsEnabled(true);
    smtpSettings.setSslEnabled(true);
    smtpSettings.setSystemEmailAddress("system@sonatype.com");
    smtpSettings.setTestEmail("system@sonatype.com");

    final SmtpSettingsResourceRequest envelope = new SmtpSettingsResourceRequest();
    envelope.setData(smtpSettings);

    client().getSubsystem(CheckSmtpSettings.class).check(envelope);

  }

  @Path("/service/local/check_smtp_settings")
  public static interface CheckSmtpSettings
      extends Restlet1xClient
  {
    @PUT
    @Produces({APPLICATION_JSON})
    void check(SmtpSettingsResourceRequest request);
  }

}
