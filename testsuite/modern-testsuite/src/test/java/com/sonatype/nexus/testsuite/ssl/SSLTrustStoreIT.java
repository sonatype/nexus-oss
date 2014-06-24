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

import java.util.Collection;

import com.sonatype.nexus.ssl.client.Certificate;

import org.sonatype.sisu.siesta.common.validation.ValidationErrorsException;

import com.sun.jersey.api.client.ClientResponse.Status;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.nexus.testsuite.support.hamcrest.UniformInterfaceExceptionMatchers.exceptionWithStatus;

/**
 * ITs related to trust store management.
 *
 * @since 1.0
 */
public class SSLTrustStoreIT
    extends SSLITSupport
{

  public SSLTrustStoreIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Before
  public void prepare() {
    // remove all certificates
    final Collection<Certificate> certificates = truststore().get();
    for (final Certificate certificate : certificates) {
      certificate.remove();
    }
  }

  /**
   * Verify that certificates can be created / removed.
   */
  @Test
  public void createReadDelete()
      throws Exception
  {
    // create a certificate
    final Certificate created = truststore().create()
        .withPem(FileUtils.readFileToString(testData().resolveFile("pem.txt")))
        .save();

    // check there are at list one certificate
    final Collection<Certificate> certificates = truststore().get();
    assertThat(certificates.size(), greaterThan(0));

    // check that we can get the created certificate
    final Certificate updated = truststore().get(created.id());
    assertThat(updated, is(equalTo(created)));

    // remove the certificate
    updated.remove();

    thrown.expect(exceptionWithStatus(Status.NOT_FOUND));

    truststore().get(created.id());
  }

  /**
   * Verify that adding a certificate with an invalid PEM results in an validation exception.
   */
  @Test
  public void createInvalidPem()
      throws Exception
  {
    thrown.expect(ValidationErrorsException.class);
    //thrown.expectMessage("Invalid PEM formatted certificate");

    truststore().create()
        .withPem("Invalid")
        .save();
  }

  /**
   * Verify that retrieving a certificate that does not exist results in an not found exception.
   */
  @Test
  public void retrieveUnknownCertificate()
      throws Exception
  {
    thrown.expect(exceptionWithStatus(Status.NOT_FOUND));
    //thrown.expectMessage("Certificate with fingerprint 'foo' not found");

    truststore().get("foo");
  }
}
