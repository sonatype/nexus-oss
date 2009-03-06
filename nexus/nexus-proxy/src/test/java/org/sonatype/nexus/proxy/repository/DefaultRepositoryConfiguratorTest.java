package org.sonatype.nexus.proxy.repository;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.SimpleApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class DefaultRepositoryConfiguratorTest extends PlexusTestCase
{

    public void testExpireNFCOnUpdate() throws Exception
    {
        RepositoryConfigurator configurator = this.lookup( RepositoryConfigurator.class );
        
        Repository oldRepository = this.lookup( Repository.class, "maven2" );
        oldRepository.setId( "test-repo" );
        
        ApplicationConfiguration configuration = new SimpleApplicationConfiguration();
        
        CRepository cRepo = new CRepository();
        cRepo.setId( "test-repo" );
        
        RemoteStorageContext rsc = new DefaultRemoteStorageContext(null);
        
        LocalRepositoryStorage ls = this.lookup( LocalRepositoryStorage.class, "file" );
        
        RemoteRepositoryStorage rs = null;
        
        oldRepository.getNotFoundCache().put( "test-path", "test-object" );
        
        // make sure the item is in NFC
        Assert.assertTrue( oldRepository.getNotFoundCache().contains( "test-path" ) );
        
        Repository newRepository = configurator.updateRepositoryFromModel( oldRepository, configuration, cRepo, rsc, ls, rs );
        
        // make sure the item is NOT in NFC
        Assert.assertFalse( newRepository.getNotFoundCache().contains( "test-path" ) );
    }
    
    public void testExpireNFCOnUpdateWithNFCDisabled() throws Exception
    {
        RepositoryConfigurator configurator = this.lookup( RepositoryConfigurator.class );
        
        Repository oldRepository = this.lookup( Repository.class, "maven2" );
        oldRepository.setId( "test-repo" );
        oldRepository.setNotFoundCacheActive( false );
        
        ApplicationConfiguration configuration = new SimpleApplicationConfiguration();
        
        CRepository cRepo = new CRepository();
        cRepo.setId( "test-repo" );
        cRepo.setNotFoundCacheActive( false );
        
        RemoteStorageContext rsc = new DefaultRemoteStorageContext(null);
        
        LocalRepositoryStorage ls = this.lookup( LocalRepositoryStorage.class, "file" );
        
        RemoteRepositoryStorage rs = null;
        
        oldRepository.getNotFoundCache().put( "test-path", "test-object" );
        
        // make sure the item is in NFC
        //(cache is disabled )
        // NOTE: we don't care if it in the cache right now, because the retrieve item does not return it.
//        Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );
        
        Repository newRepository = configurator.updateRepositoryFromModel( oldRepository, configuration, cRepo, rsc, ls, rs );
        
        // make sure the item is NOT in NFC
        Assert.assertFalse( newRepository.getNotFoundCache().contains( "test-path" ) );
    }
    
    
}
