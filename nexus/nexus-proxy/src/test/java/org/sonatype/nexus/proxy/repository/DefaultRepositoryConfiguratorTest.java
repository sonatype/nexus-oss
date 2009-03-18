package org.sonatype.nexus.proxy.repository;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.SimpleApplicationConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfigurator;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class DefaultRepositoryConfiguratorTest
    extends PlexusTestCase
{
    protected Xpp3Dom getNode( String name, String value )
    {
        Xpp3Dom node = new Xpp3Dom( name );

        node.setValue( value );

        return node;
    }

    public void testExpireNFCOnUpdate()
        throws Exception
    {
        Repository oldRepository = this.lookup( Repository.class, "maven2" );
        oldRepository.setId( "test-repo" );

        ApplicationConfiguration configuration = new SimpleApplicationConfiguration();

        CRepository cRepo = new CRepository();
        cRepo.setId( "test-repo" );
        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setProvider( "file" );
        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        ex.addChild( getNode( AbstractMavenRepositoryConfigurator.REPOSITORY_POLICY, RepositoryPolicy.RELEASE
            .toString() ) );
        cRepo.setExternalConfiguration( ex );

        RemoteStorageContext rsc = new DefaultRemoteStorageContext( null );

        LocalRepositoryStorage ls = this.lookup( LocalRepositoryStorage.class, "file" );

        RemoteRepositoryStorage rs = null;

        oldRepository.getNotFoundCache().put( "test-path", "test-object" );

        // make sure the item is in NFC
        Assert.assertTrue( oldRepository.getNotFoundCache().contains( "test-path" ) );

        oldRepository.configure( cRepo );

        // make sure the item is NOT in NFC
        Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );
    }

    public void testExpireNFCOnUpdateWithNFCDisabled()
        throws Exception
    {
        Repository oldRepository = this.lookup( Repository.class, "maven2" );
        oldRepository.setId( "test-repo" );
        oldRepository.setNotFoundCacheActive( false );

        ApplicationConfiguration configuration = new SimpleApplicationConfiguration();

        CRepository cRepo = new CRepository();
        cRepo.setId( "test-repo" );
        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        cRepo.setNotFoundCacheActive( false );
        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setProvider( "file" );
        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        ex.addChild( getNode( AbstractMavenRepositoryConfigurator.REPOSITORY_POLICY, RepositoryPolicy.RELEASE
            .toString() ) );
        cRepo.setExternalConfiguration( ex );

        RemoteStorageContext rsc = new DefaultRemoteStorageContext( null );

        LocalRepositoryStorage ls = this.lookup( LocalRepositoryStorage.class, "file" );

        RemoteRepositoryStorage rs = null;

        oldRepository.getNotFoundCache().put( "test-path", "test-object" );

        // make sure the item is in NFC
        // (cache is disabled )
        // NOTE: we don't care if it in the cache right now, because the retrieve item does not return it.
        // Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );

        oldRepository.configure( cRepo );

        // make sure the item is NOT in NFC
        Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );
    }

}
