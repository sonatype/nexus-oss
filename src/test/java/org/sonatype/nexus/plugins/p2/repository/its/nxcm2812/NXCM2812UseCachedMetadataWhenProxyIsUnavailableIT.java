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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2812;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;

public class NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT()
    {
        super( "nxcm2812" );
    }

    @Test
    public void test()
        throws IOException, Exception
    {
        final String url = "content/repositories/" + getTestRepositoryId() + "/content.xml";

        // init local storage
        final Response content = RequestFacade.sendMessage( url, Method.GET );
        Assert.assertTrue( content.getEntity().getText().contains( "<?metadataRepository" ) );
        assertInstallation();

        // invalidate remote repo
        replaceProxy();

        // check delivery from local storage
        final Response content2 = RequestFacade.sendMessage( url, Method.GET );
        Assert.assertTrue( content2.getEntity().getText().contains( "<?metadataRepository" ) );
        assertInstallation();
    }

    private void assertInstallation()
        throws IOException, Exception
    {
        final File installDir = new File( "target/eclipse/nxcm2812" );
        FileUtils.deleteDirectory( installDir );

        installUsingP2( getNexusTestRepoUrl(), "com.sonatype.nexus.p2.its.feature.feature.group",
            installDir.getCanonicalPath() );

        final File feature = new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertTrue( feature.exists() && feature.isDirectory() );

        final File bundle = new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertTrue( bundle.canRead() );
    }

    private void replaceProxy()
        throws Exception
    {
        final Handler handler = server.getServer().getHandler();
        server.stop();
        server.getServer().setHandler( new AbstractHandler()
        {

            @Override
            public void handle( final String target, final HttpServletRequest request,
                                final HttpServletResponse response, final int dispatch )
                throws IOException, ServletException
            {
                for ( final String path : P2Constants.METADATA_FILE_PATHS )
                {
                    if ( target.endsWith( path ) )
                    {
                        response.sendError( 503 );
                        return;
                    }
                }
                handler.handle( target, request, response, dispatch );
            }
        } );
        server.start();
    }

}
