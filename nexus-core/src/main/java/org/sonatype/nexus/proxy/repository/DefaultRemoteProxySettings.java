/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
