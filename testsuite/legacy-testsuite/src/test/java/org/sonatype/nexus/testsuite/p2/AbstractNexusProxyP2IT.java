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
package org.sonatype.nexus.testsuite.p2;

import static org.sonatype.nexus.test.utils.FileTestingUtils.interpolationDirectoryCopy;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.test.utils.TestProperties;

public abstract class AbstractNexusProxyP2IT
    extends AbstractNexusP2IT
{

    protected ServletServer proxyServer;

    protected static final String localStorageDir;

    static
    {
        localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
    }

    protected AbstractNexusProxyP2IT()
    {
        super();
    }

    protected AbstractNexusProxyP2IT( final String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @SuppressWarnings( "deprecation" )
    @Before
    public void startProxy()
        throws Exception
    {
        proxyServer = lookupProxyServer();
        proxyServer.start();
    }

    @After
    public void stopProxy()
        throws Exception
    {
        if ( proxyServer != null )
        {
            proxyServer.stop();
            proxyServer = null;
        }
    }

    protected ServletServer lookupProxyServer()
        throws ComponentLookupException
    {
        return (ServletServer) lookup( ServletServer.ROLE );
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
