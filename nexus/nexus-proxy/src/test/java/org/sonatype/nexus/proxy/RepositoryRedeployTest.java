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

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class RepositoryRedeployTest
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

    protected Repository getRepository()
        throws NoSuchResourceStoreException, IOException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE );
        
        getApplicationConfiguration().saveConfiguration();

        return repo1;
    }

    public void retrieveItem()
        throws Exception
    {
        StorageItem item = getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        checkForFileAndMatchContents( item );
    }

    public void testSimple()
        throws Exception
    {
        // get some initial proxied content
        retrieveItem();

        StorageFileItem item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        ResourceStoreRequest to = new ResourceStoreRequest(
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy",
            true );

        getRepository().storeItem( to, item.getInputStream(), null );

        // and repeat all this
        item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        to = new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true );

        getRepository().storeItem( to, item.getInputStream(), null );

        StorageFileItem dest = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true ) );

        checkForFileAndMatchContents( dest, getRemoteFile( getRepositoryRegistry().getRepository(
            "repo1" ), "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) );

    }

}
