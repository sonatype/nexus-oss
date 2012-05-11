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
package org.sonatype.nexus.plugin.deploy;

import java.io.File;

public class ZapperRequest
{
    private final File stageRepository;

    private final String remoteUrl;

    private String remoteUsername;

    private String remotePassword;

    private String proxyProtocol;

    private String proxyHost;

    private int proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    public ZapperRequest( File stageRepository, String remoteUrl )
    {
        this.stageRepository = stageRepository;
        this.remoteUrl = remoteUrl;
    }

    protected String getRemoteUsername()
    {
        return remoteUsername;
    }

    protected void setRemoteUsername( String remoteUsername )
    {
        this.remoteUsername = remoteUsername;
    }

    protected String getRemotePassword()
    {
        return remotePassword;
    }

    protected void setRemotePassword( String remotePassword )
    {
        this.remotePassword = remotePassword;
    }

    protected String getProxyProtocol()
    {
        return proxyProtocol;
    }

    protected void setProxyProtocol( String proxyProtocol )
    {
        this.proxyProtocol = proxyProtocol;
    }

    protected String getProxyHost()
    {
        return proxyHost;
    }

    protected void setProxyHost( String proxyHost )
    {
        this.proxyHost = proxyHost;
    }

    protected int getProxyPort()
    {
        return proxyPort;
    }

    protected void setProxyPort( int proxyPort )
    {
        this.proxyPort = proxyPort;
    }

    protected String getProxyUsername()
    {
        return proxyUsername;
    }

    protected void setProxyUsername( String proxyUsername )
    {
        this.proxyUsername = proxyUsername;
    }

    protected String getProxyPassword()
    {
        return proxyPassword;
    }

    protected void setProxyPassword( String proxyPassword )
    {
        this.proxyPassword = proxyPassword;
    }

    protected File getStageRepository()
    {
        return stageRepository;
    }

    protected String getRemoteUrl()
    {
        return remoteUrl;
    }
}
