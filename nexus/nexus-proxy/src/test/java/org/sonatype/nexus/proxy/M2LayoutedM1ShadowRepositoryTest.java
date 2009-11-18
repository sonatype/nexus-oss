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

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven1.M1Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

public class M2LayoutedM1ShadowRepositoryTest
    extends AbstractProxyTestEnvironment
{
    private static final long A_DAY = 24L * 60L * 60L * 1000L;
    
    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M1TestsuiteEnvironmentBuilder( ss );
    }

    protected void addShadowReposes()
        throws ConfigurationException,
            IOException,
            ComponentLookupException
    {
        String masterId = "repo1-m1";
        
            M2LayoutedM1ShadowRepository shadow = (M2LayoutedM1ShadowRepository) getContainer().lookup(
                ShadowRepository.class,
                "m1-m2-shadow" );

            CRepository repoConf = new DefaultCRepository();

            repoConf.setProviderRole( ShadowRepository.class.getName() );
            repoConf.setProviderHint( "m1-m2-shadow" );
            repoConf.setId( masterId + "-m2" );
            repoConf.setIndexable( false );

            repoConf.setLocalStorage( new CLocalStorage() );
            repoConf.getLocalStorage().setProvider( "file" );

            Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
            repoConf.setExternalConfiguration( exRepo );
            M1LayoutedM2ShadowRepositoryConfiguration exRepoConf = new M1LayoutedM2ShadowRepositoryConfiguration(
                exRepo );
            exRepoConf.setMasterRepositoryId( masterId );

            shadow.configure( repoConf );

            shadow.synchronizeWithMaster();

            getRepositoryRegistry().addRepository( shadow );

    }

    

    public void testProxyLastRequestedAttribute() throws Exception
    {
        this.addShadowReposes();
        
        M2LayoutedM1ShadowRepository shadowRepository = (M2LayoutedM1ShadowRepository) this.getRepositoryRegistry().getRepository( "repo1-m1-m2" );
        M1Repository m1Repository = (M1Repository) this.getRepositoryRegistry().getRepository( "repo1-m1" );
        
        String originalPath = "activeio/poms/activeio-2.1.pom";
        String shadowPath = "activeio/activeio/2.1/activeio-2.1.pom";
        ResourceStoreRequest request = new ResourceStoreRequest( shadowPath );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        ResourceStoreRequest m1Request = new ResourceStoreRequest( originalPath );
        shadowRepository.retrieveItem( request );
        long lastRequest =  System.currentTimeMillis() - 10 * A_DAY;
        
        // FIXME: the path of the storage item is wrong, so we need to fix it in order to save the file again
        // and i think it should be a linked storage item, not a default one
        AbstractStorageItem originalShadowStorageItem = shadowRepository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( shadowRepository.createUid( request.getRequestPath() ) );
        originalShadowStorageItem.setLastRequested( lastRequest );
        shadowRepository.getLocalStorage().getAttributesHandler().getAttributeStorage().putAttribute( originalShadowStorageItem );

        // i should not need to update both sets of attributes, but I am going to....
        AbstractStorageItem originalStorageItem = m1Repository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( m1Repository.createUid( m1Request.getRequestPath() ) );
        originalStorageItem.setLastRequested( lastRequest );
        m1Repository.getLocalStorage().getAttributesHandler().getAttributeStorage().putAttribute( originalStorageItem );
        
        // now request the object, the lastRequested timestamp should be updated
        // this one will be a link
        StorageLinkItem resultItemLink = (StorageLinkItem) shadowRepository.retrieveItem( request );
        StorageItem resultStorageItem = this.dereferenceLink( resultItemLink );

        Assert.assertTrue( resultItemLink.getLastRequested() > lastRequest );
        Assert.assertTrue( resultStorageItem.getLastRequested() > lastRequest );

        Assert.assertTrue( resultStorageItem.getLastRequested() >= resultItemLink.getLastRequested() );
        
        // check the shadow attributes
        AbstractStorageItem shadowStorageItem = shadowRepository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( shadowRepository.createUid( request.getRequestPath() ) );
        Assert.assertEquals( resultItemLink.getLastRequested(), shadowStorageItem.getLastRequested() );
        
        // check the original attributes
        AbstractStorageItem m1StorageItem = m1Repository.getLocalStorage().getAttributesHandler().getAttributeStorage().getAttributes( m1Repository.createUid( m1Request.getRequestPath() ) );
        Assert.assertEquals( resultStorageItem.getLastRequested(), m1StorageItem.getLastRequested() );
        
    }
    
    public StorageItem dereferenceLink( StorageLinkItem link )
    throws AccessDeniedException, ItemNotFoundException, IllegalOperationException, StorageException
    {
        ResourceStoreRequest req = new ResourceStoreRequest( link.getTarget().getPath() );
        req.getRequestContext().putAll( link.getItemContext() );
//        req.getRequestContext().setParentContext( link.getItemContext().getParentContext() );
    
        return link.getTarget().getRepository().retrieveItem( req );
    }
}