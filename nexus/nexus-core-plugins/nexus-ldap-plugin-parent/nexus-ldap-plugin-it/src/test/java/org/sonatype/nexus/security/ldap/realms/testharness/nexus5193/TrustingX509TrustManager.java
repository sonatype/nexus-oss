/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nexus5193;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

/**
* A very naive TrustManager.
*/
class TrustingX509TrustManager
    extends X509ExtendedTrustManager
{

    @Override
    public void checkClientTrusted( final X509Certificate[] x509Certificates, final String s, final String s1,
                                    final String s2 )
        throws CertificateException
    {
    }

    @Override
    public void checkServerTrusted( final X509Certificate[] x509Certificates, final String s, final String s1,
                                    final String s2 )
        throws CertificateException
    {
    }

    @Override
    public void checkClientTrusted( final X509Certificate[] x509Certificates, final String s )
        throws CertificateException
    {
    }

    @Override
    public void checkServerTrusted( final X509Certificate[] x509Certificates, final String s )
        throws CertificateException
    {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }
}
