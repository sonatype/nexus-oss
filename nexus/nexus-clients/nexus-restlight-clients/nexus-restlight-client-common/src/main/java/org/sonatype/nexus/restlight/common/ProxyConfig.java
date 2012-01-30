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
package org.sonatype.nexus.restlight.common;

/**
 * Immutable basic proxy server configuration.
 * <p>
 * A port value of -1 is equivalent to an unspecified port.
 *
 * @since 1.9.2.3
 * @since 2.0
 */
public class ProxyConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    /**
     * Equivalent to {@code ProxyConfig(host, -1, null, null)}
     *
     * @param host the proxy host name
     */
    public ProxyConfig(String host)
    {
        this(host, -1 ,null,null);
    }

    /**
     * Equivalent to {@code ProxyConfig(host, port, null, null)}
     *
     * @param host the proxy host name
     * @param port the proxy port, or -1 to unspecified port
     */
    public ProxyConfig(String host, int port)
    {
        this(host, port, null, null);
    }


    /**
     * Construct a basic proxy configuration model.
     * <p>
     * A negative one (-1) port value is equivalent to an unspecified port.
     *
     * @param host the proxy host name
     * @param port the proxy port, or -1 to unspecified port
     * @param username the optional username for proxy authentication
     * @param password the optional password for proxy authentication
     *
     * @throws NullPointerException host is null
     * @throws IllegalArgumentException if port is not -1 or between 1 and 65535 inclusive.
     */
    public ProxyConfig(final String host, final int port, final String username, final String password)
    {
        if(host == null)
        {
            throw new NullPointerException("host is required");
        }
        this.host = host;

        if(port < -1 || port == 0 || port > 65535)
        {
            throw new IllegalArgumentException("port must be -1 or between 1 and 65535 inclusive.");
        }
        this.port = port;

        this.username = username;
        this.password = password;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getPassword()
    {
        return password;
    }

    public String getUsername()
    {
        return username;
    }

}
