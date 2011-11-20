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
package org.sonatype.nexus.plugins.p2.repository.its;

import static org.sonatype.nexus.test.utils.FileTestingUtils.interpolationDirectoryCopy;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractNexusProxyP2IT
    extends AbstractNexusP2IT
{

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    protected static ServletServer proxyServer;

    protected static final String localStorageDir;

    static
    {
        localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
    }

    protected AbstractNexusProxyP2IT( final String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @BeforeClass( alwaysRun = true )
    public void startProxy()
        throws Exception
    {
        proxyServer = (ServletServer) lookup( ServletServer.ROLE );
        proxyServer.start();
    }

    @AfterClass( alwaysRun = true )
    public void stopProxy()
        throws Exception
    {
        if ( proxyServer != null )
        {
            proxyServer.stop();
            proxyServer = null;
        }
    }

    protected void replaceInFile( final String filename, final String target, final String replacement )
        throws IOException
    {
        String content = FileUtils.fileRead( filename );
        content = content.replace( target, replacement );
        FileUtils.fileWrite( filename, content );
    }

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        final File source = new File( TestProperties.getString( "test.resources.source.folder" ), "proxyRepo" );
        if ( !source.exists() )
        {
            return;
        }

        interpolationDirectoryCopy( source, new File( localStorageDir ), TestProperties.getAll() );
    }

}
