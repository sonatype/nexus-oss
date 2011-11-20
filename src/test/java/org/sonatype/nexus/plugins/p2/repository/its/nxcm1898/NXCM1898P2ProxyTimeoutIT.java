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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1898;

import java.io.File;
import java.io.IOException;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class NXCM1898P2ProxyTimeoutIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1898P2ProxyTimeoutIT()
    {
        super( "nxcm1898" );
        // System.setProperty( "org.eclipse.ecf.provider.filetransfer.retrieve.readTimeout", "30000" );
    }

    @Override
    @BeforeClass( alwaysRun = true )
    public void startProxy()
        throws Exception
    {
        proxyServer = (ServletServer) lookup( ServletServer.ROLE, "timeout" );
        proxyServer.start();
    }

    @Test( enabled = false )
    protected void test( final int timeout )
        throws IOException, Exception
    {
        final String nexusTestRepoUrl = getNexusTestRepoUrl();

        final File installDir = new File( "target/eclipse/nxcm1898" );

        // give it a good amount of time
        final GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.getGlobalConnectionSettings().setConnectionTimeout( timeout );
        SettingsMessageUtil.save( settings );

        installAndVerifyP2Feature();
    }

}