/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.restlight.common;

/**
 * Immutable basic proxy server configuration.
 * <p>
 * A port value of -1 is equivalent to an unspecified port.
 *
 * @since 1.9.2.3
 * @since 1.9.3
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
