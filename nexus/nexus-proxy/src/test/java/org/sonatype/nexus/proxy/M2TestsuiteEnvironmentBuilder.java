/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.jettytestsuite.WebappContext;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

/**
 * The Class JettyTestsuiteEnvironment.
 * 
 * @author cstamas
 */
public class M2TestsuiteEnvironmentBuilder
    extends AbstractJettyEnvironmentBuilder
{

    public M2TestsuiteEnvironmentBuilder( ServletServer servletServer )
    {
        super( servletServer );
    }

    public void buildEnvironment( AbstractProxyTestEnvironment env )
        throws IOException,
            ComponentLookupException
    {
        PlexusContainer container = env.getPlexusContainer();

        List<String> reposes = new ArrayList<String>();
        for ( WebappContext remoteRepo : getServletServer().getWebappContexts() )
        {
            M2Repository repo = (M2Repository) container.lookup( Repository.ROLE, "maven2" );

            // repo.enableLogging( env.getLogger().getChildLogger( "REPO" + repo.getId() ) );
            repo.setId( remoteRepo.getName() );
            repo.setRemoteUrl( getServletServer().getUrl( remoteRepo.getName() ) );
            repo.setLocalUrl( env
                .getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + repo.getId() ).toURI().toURL()
                .toString() );
            repo.setLocalStorage( env.getLocalRepositoryStorage() );
            repo.setRepositoryPolicy( RepositoryPolicy.RELEASE );
            repo.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );
            // repo.setFeedRecorder( new SimpleFeedRecorder() );
            if ( remoteRepo.getAuthenticationInfo() != null )
            {
                // we have a protected repo, cannot share remote peer
                // auth should be set somewhere else
                CommonsHttpClientRemoteStorage rs = new CommonsHttpClientRemoteStorage();
                rs.enableLogging( env.getLogger().getChildLogger( "RS" + repo.getId() ) );
                repo.setRemoteStorage( rs );
                repo.setRemoteStorageContext( new DefaultRemoteStorageContext( env.getRemoteStorageContext() ) );
            }
            else
            {
                repo.setRemoteStorage( env.getRemoteRepositoryStorage() );

                repo.setRemoteStorageContext( env.getRemoteStorageContext() );
            }
            // repo.setCacheManager( env.getCacheManager() );
            reposes.add( repo.getId() );

            env.getRepositoryRegistry().addRepository( repo );
        }

        // ading one hosted only
        M2Repository repo = (M2Repository) container.lookup( Repository.ROLE, "maven2" );
        // repo.enableLogging( env.getLogger().getChildLogger( "REPO" + repo.getId() ) );
        repo.setId( "inhouse" );
        repo.setLocalUrl( env
            .getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + repo.getId() ).toURI().toURL()
            .toString() );
        repo.setLocalStorage( env.getLocalRepositoryStorage() );
        // repo.setCacheManager( env.getCacheManager() );
        reposes.add( repo.getId() );
        env.getRepositoryRegistry().addRepository( repo );

        try
        {
            env.getRepositoryRegistry().addRepositoryGroup( "test", reposes );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new IllegalStateException( "Hum hum", e );
        }
        catch ( InvalidGroupingException e )
        {
            throw new IllegalStateException( "Hum hum", e );
        }

        // adding routers
    }

}
