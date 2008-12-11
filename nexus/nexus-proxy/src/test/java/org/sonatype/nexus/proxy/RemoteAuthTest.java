/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class RemoteAuthTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testHttpAuths()
        throws Exception
    {
        // remote target of repo1 is not protected
        StorageItem item = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
            new ResourceStoreRequest( "/repo1.txt", false ) );
        checkForFileAndMatchContents( item );

        // remote target of repo2 is protected with HTTP BASIC
        CRemoteAuthentication dras2 = new CRemoteAuthentication();
        dras2.setUsername( "cstamas" );
        dras2.setPassword( "cstamas123" );
        DefaultRemoteStorageContext ctx2 = new DefaultRemoteStorageContext( null );
        ctx2.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS, dras2 );
        getRepositoryRegistry().getRepository( "repo2" ).setRemoteStorageContext( ctx2 );

        item = getRepositoryRegistry().getRepository( "repo2" ).retrieveItem(
            new ResourceStoreRequest( "/repo2.txt", false ) );
        checkForFileAndMatchContents( item );

        // remote target of repo3 is protected with HTTP DIGEST
        CRemoteAuthentication dras3 = new CRemoteAuthentication();
        dras3.setUsername( "brian" );
        dras3.setPassword( "brian123" );
        DefaultRemoteStorageContext ctx3 = new DefaultRemoteStorageContext( null );
        ctx3.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS, dras3 );
        getRepositoryRegistry().getRepository( "repo3" ).setRemoteStorageContext( ctx3 );

        item = getRepositoryRegistry().getRepository( "repo3" ).retrieveItem(
            new ResourceStoreRequest( "/repo3.txt", false ) );
        checkForFileAndMatchContents( item );
    }
}
