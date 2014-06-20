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

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.port.PortReservationService;

import org.junit.Test;

import static com.sonatype.nexus.ssl.model.SMTPTrustStoreKey.smtpTrustStoreKey;
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

}
