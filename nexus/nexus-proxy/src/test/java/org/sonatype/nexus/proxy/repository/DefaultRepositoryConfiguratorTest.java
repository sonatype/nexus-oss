package org.sonatype.nexus.proxy.repository;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;

public class DefaultRepositoryConfiguratorTest
    extends PlexusTestCase
{
    public void testExpireNFCOnUpdate()
        throws Exception
    {
        Repository oldRepository = this.lookup( Repository.class, "maven2" );

        CRepository cRepo = new DefaultCRepository();
        cRepo.setId( "test-repo" );
        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        cRepo.setNotFoundCacheTTL( 1 );
        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setProvider( "file" );
        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        cRepo.setExternalConfiguration( ex );
        M2RepositoryConfiguration extConf = new M2RepositoryConfiguration( ex );
        extConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        oldRepository.configure( cRepo );

        oldRepository.getNotFoundCache().put( "test-path", "test-object" );

        // make sure the item is in NFC
        Assert.assertTrue( oldRepository.getNotFoundCache().contains( "test-path" ) );

        // change config
        cRepo.setNotFoundCacheTTL( 2 );

        oldRepository.configure();

        // make sure the item is NOT in NFC
        Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );
    }

    public void testExpireNFCOnUpdateWithNFCDisabled()
        throws Exception
    {
        Repository oldRepository = this.lookup( Repository.class, "maven2" );

        CRepository cRepo = new DefaultCRepository();
        cRepo.setId( "test-repo" );
        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        cRepo.setNotFoundCacheTTL( 1 );
        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setProvider( "file" );
        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        cRepo.setExternalConfiguration( ex );
        M2RepositoryConfiguration extConf = new M2RepositoryConfiguration( ex );
        extConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        oldRepository.configure( cRepo );

        oldRepository.getNotFoundCache().put( "test-path", "test-object" );

        // make sure the item is in NFC
        // (cache is disabled )
        // NOTE: we don't care if it in the cache right now, because the retrieve item does not return it.
        // Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );

        oldRepository.configure();

        // make sure the item is NOT in NFC
        Assert.assertFalse( oldRepository.getNotFoundCache().contains( "test-path" ) );
    }

}
