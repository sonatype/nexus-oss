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
package org.sonatype.nexus.plugins.rrb;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.AbstractPluginTestCase;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;
import org.sonatype.plexus.rest.resource.PlexusResource;

public class RemoteBrowserResourceAuthTest
    extends AbstractPluginTestCase
{
    private ServletServer server = null;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.server = this.lookup( ServletServer.class );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        int port = 0;
        ServerSocket socket = null;
        try
        {
            socket = new ServerSocket( 0 );
            port = socket.getLocalPort();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            Assert.fail( "Could not find free port: " + e.getMessage() );
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                Assert.fail( "Could not close socket: " + e.getMessage() );
            }
        }

        context.put( "jetty-port", Integer.toString( port ) );
        context.put( "resource-base", "target" );

    }

    public void testSiteWithAuth()
        throws Exception
    {
        String remoteUrl = server.getUrl( "auth-test/" );

        String repoId = "testSiteWithAuth";
        RepositoryRegistry repoRegistry = this.lookup( RepositoryRegistry.class );

        TemplateProvider templateProvider =
            this.lookup( TemplateProvider.class, DefaultRepositoryTemplateProvider.PROVIDER_ID );
        Maven2ProxyRepositoryTemplate template =
            (Maven2ProxyRepositoryTemplate) templateProvider.getTemplateById( "default_proxy_release" );
        template.getCoreConfiguration().getConfiguration( true ).setId( repoId );
        template.getCoreConfiguration().getConfiguration( true ).setName( repoId + "-name" );
        template.getCoreConfiguration().getConfiguration( true ).setIndexable( false ); // disable index
        template.getCoreConfiguration().getConfiguration( true ).setSearchable( false ); // disable index

        M2Repository m2Repo = (M2Repository) template.create();
        repoRegistry.addRepository( m2Repo );

        m2Repo.setRemoteUrl( remoteUrl );
        m2Repo.setRemoteAuthenticationSettings( new UsernamePasswordRemoteAuthenticationSettings( "admin", "admin" ) );
        m2Repo.commitChanges();
        
        Reference rootRef = new Reference( "http://localhost:8081/nexus/service/local/repositories/" + repoId + "" );
        Reference resourceRef = new Reference( rootRef, "http://localhost:8081/nexus/service/local/repositories/" + repoId + "/" );

        // now call the REST resource
        Request request = new Request();
        request.setRootRef( rootRef );
        request.setOriginalRef( rootRef );
        request.setResourceRef( resourceRef );
        request.getAttributes().put( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY, repoId );
        Form form = new Form();
        form.add( "Accept", "application/json" );
        form.add( "Referer", "http://localhost:8081/nexus/index.html#view-repositories;"+repoId );
        form.add( "Host", " localhost:8081" );
        request.getAttributes().put( "org.restlet.http.headers", form);

         PlexusResource plexusResource = this.lookup( PlexusResource.class, RemoteBrowserResource.class.getName() );
         String jsonString = plexusResource.get( null, request, null, null ).toString();
        
         // TODO: do some better validation then this
         Assert.assertTrue( jsonString.contains( "/auth-test/classes/" ) );
         Assert.assertTrue( jsonString.contains( "/auth-test/test-classes/" ) );

    }

    @Override
    protected void tearDown()
        throws Exception
    {

        if ( this.server != null )
        {
            this.server.stop();
        }

        super.tearDown();
    }

}
