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
package org.sonatype.nexus.proxy.repository;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;

public class DefaultRemoteProxySettings
    implements RemoteProxySettings
{
    private boolean blockInheritance;

    private String hostname;

    private int port;
    
    private Set<String> nonProxyHosts = new HashSet<String>();

    private RemoteAuthenticationSettings proxyAuthentication;

    public boolean isEnabled()
    {
        return StringUtils.isNotBlank( getHostname() ) && getPort() != 0;
    }

    public boolean isBlockInheritance()
    {
        return blockInheritance;
    }

    public void setBlockInheritance( boolean blockInheritance )
    {
        this.blockInheritance = blockInheritance;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname( String hostname )
    {
        this.hostname = hostname;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public RemoteAuthenticationSettings getProxyAuthentication()
    {
        return proxyAuthentication;
    }

    public void setProxyAuthentication( RemoteAuthenticationSettings proxyAuthentication )
    {
        this.proxyAuthentication = proxyAuthentication;
    }

    public Set<String> getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public void setNonProxyHosts( Set<String> nonProxyHosts )
    {
        this.nonProxyHosts = nonProxyHosts;
    }
}
