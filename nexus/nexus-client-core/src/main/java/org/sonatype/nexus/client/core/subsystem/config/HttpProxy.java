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
package org.sonatype.nexus.client.core.subsystem.config;

/**
 * Http Proxy configuration sub/subsystem.
 *
 * @since 2.2
 */
public interface HttpProxy
{

    /**
     * Configure Nexus to use the specified HTTP proxy.
     *
     * @param host          host name/ip of HTTP proxy to be used for remote connections. Cannot be null/empty.
     * @param port          port of HTTP proxy to be used for remote connections.
     * @param nonProxyHosts (optional) list of hosts for which the proxy should not be used.
     */
    void setTo( String host, int port, String... nonProxyHosts );

    /**
     * Configure Nexus to use the specified credentials to authenticate against HTTP proxy.
     *
     * @param username to use to authenticate against HTTP proxy. Cannot be null/empty.
     * @param password to use to authenticate against HTTP proxy.
     */
    void setCredentials( String username, String password );

    /**
     * Configure Nexus to use the specified credentials to authenticate against HTTP proxy.
     *
     * @param username   to use to authenticate against HTTP proxy. Cannot be null/empty.
     * @param password   to use to authenticate against HTTP proxy.
     * @param ntlmHost   NTLM host.
     * @param ntlmDomain NTLM domain.
     */
    void setCredentials( String username, String password, String ntlmHost, String ntlmDomain );

    /**
     * Configure Nexus to not use an HTTP proxy.
     */
    void reset();

}
