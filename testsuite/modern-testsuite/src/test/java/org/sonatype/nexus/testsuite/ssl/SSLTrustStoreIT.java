/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.ssl;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;

import javax.inject.Inject;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.testsuite.NexusCoreITSupport;
import org.sonatype.sisu.goodies.ssl.keystore.KeyNotFoundException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * ITs related to trust store management.
 */
@ExamReactorStrategy(PerClass.class)
public class SSLTrustStoreIT
    extends NexusCoreITSupport
{
  @Inject
  private TrustStore trustStore;

  /**
   * Verify that certificates can be created / removed.
   */
  @Test
  public void createReadDelete() throws Exception {
    // create a certificate
    final Certificate created = trustStore.importTrustCertificate(
        FileUtils.readFileToString(testData.resolveFile("pem.txt")), "test");

    // check that we can get the created certificate
    final Certificate updated = trustStore.getTrustedCertificate("test");
    assertThat(updated, is(equalTo(created)));

    // remove the certificate
    trustStore.removeTrustCertificate("test");

    // check it no longer exists
    thrown.expect(KeyNotFoundException.class);
    trustStore.getTrustedCertificate("test");
  }

  /**
   * Verify that adding a certificate with an invalid PEM results in an validation exception.
   */
  @Test
  public void createInvalidPem() throws Exception {
    thrown.expect(CertificateParsingException.class);
    trustStore.importTrustCertificate("Invalid", "test");
  }

  /**
   * Verify that retrieving a certificate that does not exist results in an exception.
   */
  @Test
  public void retrieveUnknownCertificate() throws Exception {
    thrown.expect(KeyNotFoundException.class);
    trustStore.getTrustedCertificate("foo");
  }
}
