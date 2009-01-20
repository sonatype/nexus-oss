/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
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
            M2Repository repo = (M2Repository) container.lookup( Repository.class, "maven2" );

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
                CommonsHttpClientRemoteStorage rs = (CommonsHttpClientRemoteStorage) env.getPlexusContainer().lookup(
                    RemoteRepositoryStorage.class,
                    "apacheHttpClient3x" );
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
        M2Repository repo = (M2Repository) container.lookup( Repository.class, "maven2" );

        // repo.enableLogging( env.getLogger().getChildLogger( "REPO" + repo.getId() ) );
        repo.setId( "inhouse" );
        repo.setLocalUrl( env
            .getApplicationConfiguration().getWorkingDirectory( "proxy/store/" + repo.getId() ).toURI().toURL()
            .toString() );
        repo.setLocalStorage( env.getLocalRepositoryStorage() );
        // repo.setCacheManager( env.getCacheManager() );
        reposes.add( repo.getId() );
        env.getRepositoryRegistry().addRepository( repo );

        // add a hosted snapshot repo
        M2Repository repoSnapshot = (M2Repository) container.lookup( Repository.class, "maven2" );

        repoSnapshot.setId( "inhouse-snapshot" );
        repoSnapshot.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repoSnapshot.setLocalUrl( env.getApplicationConfiguration().getWorkingDirectory(
            "proxy/store/" + repoSnapshot.getId() ).toURI().toURL().toString() );
        repoSnapshot.setLocalStorage( env.getLocalRepositoryStorage() );
        reposes.add( repoSnapshot.getId() );
        env.getRepositoryRegistry().addRepository( repoSnapshot );

        M2GroupRepository group = (M2GroupRepository) container.lookup( GroupRepository.class, "maven2" );

        group.setId( "test" );

        group.setLocalUrl( env
            .getApplicationConfiguration().getWorkingDirectory( "proxy/groupstore/" + repo.getId() ).toURI().toURL()
            .toString() );

        group.setLocalStorage( env.getLocalRepositoryStorage() );

        group.setMemberRepositories( reposes );

        env.getRepositoryRegistry().addRepository( group );

        // adding routers
    }
}
