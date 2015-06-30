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
package org.sonatype.nexus.ldap.internal;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.util.Collection;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.sisu.goodies.ssl.keystore.KeystoreException;

/**
 * Mock {@link TrustStore}.
 *
 * @since 2.4
 */
@Named("default")
@Singleton
public class MockTrustStore
    implements TrustStore
{

  @Override
  public Certificate importTrustCertificate(final Certificate certificate, final String alias)
      throws KeystoreException
  {
    throw new IllegalStateException("Call not expected");
  }

  @Override
  public Certificate importTrustCertificate(final String certificateInPEM, final String alias)
      throws KeystoreException, CertificateParsingException
  {
    throw new IllegalStateException("Call not expected");
  }

  @Override
  public Certificate getTrustedCertificate(final String alias)
      throws KeystoreException
  {
    throw new IllegalStateException("Call not expected");
  }

  @Override
  public Collection<Certificate> getTrustedCertificates()
      throws KeystoreException
  {
    throw new IllegalStateException("Call not expected");
  }

  @Override
  public void removeTrustCertificate(final String alias)
      throws KeystoreException
  {
    throw new IllegalStateException("Call not expected");
  }

  @Override
  public SSLContext getSSLContext() {
    return null;
  }

}
