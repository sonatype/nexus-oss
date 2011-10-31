/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractNexusProxyIntegrationTest
    extends AbstractNexusIntegrationTest
{

    protected String baseProxyURL = null;

    protected String localStorageDir = null;

    protected Integer proxyPort;
    
    protected ServletServer proxyServer = null;

    protected final RepositoryMessageUtil repositoryUtil;

    protected AbstractNexusProxyIntegrationTest()
    {
        this( "release-proxy-repo-1" );
    }

    protected AbstractNexusProxyIntegrationTest( String testRepositoryId )
    {
        super( testRepositoryId );

        this.baseProxyURL = TestProperties.getString( "proxy.repo.base.url" );
        this.localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
        this.proxyPort = TestProperties.getInteger( "proxy.server.port" );

        this.repositoryUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @BeforeMethod(alwaysRun = true)
    public void startProxy()
        throws Exception
    {
        this.proxyServer = (ServletServer) this.lookup( ServletServer.ROLE );
        this.proxyServer.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopProxy()
        throws Exception
    {
        if( this.proxyServer != null )
        {
            this.proxyServer.stop();
        }
    }

    public File getLocalFile( String repositoryId, Gav gav )
    {
        return this.getLocalFile( repositoryId, gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                  gav.getExtension() );
    }

    public File getLocalFile( String repositoryId, String groupId, String artifact, String version, String type )
    {
        File result =
            new File( this.localStorageDir, repositoryId + "/" + groupId.replace( '.', '/' ) + "/" + artifact + "/"
                + version + "/" + artifact + "-" + version + "." + type );
        log.debug( "Returning file: " + result );
        return result;
    }

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        File source = new File( TestProperties.getString( "test.resources.source.folder" ), "proxyRepo" );
        if ( !source.exists() )
        {
            return;
        }

        FileTestingUtils.interpolationDirectoryCopy( source, new File( localStorageDir ), getTestProperties() );

    }
}
