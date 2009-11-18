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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven1.M1Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class M1RepositoryTest
    extends M1ResourceStoreTest
{
    private static final long A_DAY = 24L * 60L * 60L * 1000L;
    
    protected static final String SPOOF_RELEASE = "/spoof/poms/spoof-1.0.pom";

    protected static final String SPOOF_SNAPSHOT = "/spoof/poms/spoof-1.0-SNAPSHOT.pom";

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M1TestsuiteEnvironmentBuilder( ss );
    }

    @Override
    protected String getItemPath()
    {
        return "/activeio/jars/activeio-2.1.jar";
    }

    @Override
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException, IOException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1-m1" );

        repo1.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE );
        
        getApplicationConfiguration().saveConfiguration();

        return repo1;
    }

    public void testPoliciesWithRetrieve()
        throws Exception
    {
        M1Repository repository = (M1Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        repository.getCurrentCoreConfiguration().commitChanges();

        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );

            fail( "Should not be able to get snapshot from release repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // reset NFC
        repository.expireCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repository.getCurrentCoreConfiguration().commitChanges();

        item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );

            fail( "Should not be able to get release from snapshot repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    public void testPoliciesWithStore()
        throws Exception
    {
        M1Repository repository = (M1Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        repository.getCurrentCoreConfiguration().commitChanges();

        DefaultStorageFileItem item = new DefaultStorageFileItem(
            repository,
            SPOOF_RELEASE,
            true,
            true,
            new StringContentLocator( SPOOF_RELEASE ) );

        repository.storeItem( false, item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
                SPOOF_SNAPSHOT ) );

            repository.storeItem( false, item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }

        // reset NFC
        repository.expireCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        repository.getCurrentCoreConfiguration().commitChanges();

        item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
            SPOOF_SNAPSHOT ) );

        repository.storeItem( false, item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new StringContentLocator(
                SPOOF_RELEASE ) );

            repository.storeItem( false, item );

            fail( "Should not be able to store release to snapshot repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }
    }

    public void testProxyLastRequestedAttribute() throws Exception
    {
        M1Repository repository = (M1Repository) this.getRepositoryRegistry().getRepository( "repo1-m1" );
        
        String item = "/spoof/poms/spoof-1.0.pom";
        ResourceStoreRequest request = new ResourceStoreRequest( item );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        StorageItem storageItem = repository.retrieveItem( request );
        long lastRequest =  System.currentTimeMillis() - 10 * A_DAY;
        storageItem.setLastRequested( lastRequest );
        repository.storeItem( false, storageItem );
        
        // now request the object, the lastRequested timestamp should be updated
        StorageItem resultItem = repository.retrieveItem( request );
        Assert.assertTrue( resultItem.getLastRequested() + " > " + lastRequest, resultItem.getLastRequested() > lastRequest );
        
        // check the shadow attributes
        AbstractStorageItem shadowStorageItem = repository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( repository.createUid( request.getRequestPath() ) );
        Assert.assertEquals( resultItem.getLastRequested(), shadowStorageItem.getLastRequested() );
    }
    
    public void testHostedLastRequestedAttribute() throws Exception
    {
        String itemPath = "/spoof/poms/spoof-1.0.pom";
        
        M1Repository repository = (M1Repository) this.getRepositoryRegistry().getRepository( "inhouse" );
        File inhouseLocalStorageDir = new File( new URL(((CRepositoryCoreConfiguration) repository.getCurrentCoreConfiguration()).getConfiguration( false).getLocalStorage().getUrl() ).getFile());
        
        File artifactFile = new File( inhouseLocalStorageDir, itemPath );
        artifactFile.getParentFile().mkdirs();
        
        FileUtils.fileWrite( artifactFile.getAbsolutePath(), "Some Text so the file is not empty" );
        
        ResourceStoreRequest request = new ResourceStoreRequest( itemPath );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        StorageItem storageItem = repository.retrieveItem( request );
        long lastRequest =  System.currentTimeMillis() - 10 * A_DAY;
        storageItem.setLastRequested( lastRequest );
        repository.storeItem( false, storageItem );
        
        // now request the object, the lastRequested timestamp should be updated
        StorageItem resultItem = repository.retrieveItem( request );
        Assert.assertTrue( resultItem.getLastRequested() > lastRequest );
        
        // check the shadow attributes
        AbstractStorageItem shadowStorageItem = repository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( repository.createUid( request.getRequestPath() ) );
        Assert.assertEquals( resultItem.getLastRequested(), shadowStorageItem.getLastRequested() );
    }
    
}
