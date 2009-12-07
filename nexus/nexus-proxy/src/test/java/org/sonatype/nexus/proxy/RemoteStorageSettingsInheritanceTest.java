package org.sonatype.nexus.proxy;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Here we test how RemoteStorageContext changes (http proxy, connection settings) are propagating, and are they
 * propagating correctly from proxy repo to global if set/changed.
 * 
 * @author cstamas
 */
public class RemoteStorageSettingsInheritanceTest
    extends AbstractProxyTestEnvironment
{
    protected ApplicationConfiguration applicationConfiguration;

    protected ProxyRepository aProxyRepository;

    public void setUp()
        throws Exception
    {
        super.setUp();

        applicationConfiguration = lookup( ApplicationConfiguration.class );

        aProxyRepository = lookup( RepositoryRegistry.class ).getRepositoryWithFacet( "central", ProxyRepository.class );
    }

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        return new EnvironmentBuilder()
        {
            public void stopService()
                throws Exception
            {
                // nothing
            }

            public void startService()
                throws Exception
            {
                // nothing
            }

            public void buildEnvironment( AbstractProxyTestEnvironment abstractProxyTestEnvironment )
                throws ConfigurationException, IOException, ComponentLookupException
            {
                // building a "mock" proxy repo we will not use at all
                M2Repository repo = (M2Repository) getContainer().lookup( Repository.class, "maven2" );

                CRepository repoConf = new DefaultCRepository();

                repoConf.setProviderRole( Repository.class.getName() );
                repoConf.setProviderHint( "maven2" );
                repoConf.setId( "central" );
                repoConf.setName( "Fake Central" );

                repoConf.setLocalStorage( new CLocalStorage() );
                repoConf.getLocalStorage().setProvider( "file" );
                repoConf.getLocalStorage().setUrl(
                    abstractProxyTestEnvironment.getApplicationConfiguration().getWorkingDirectory(
                        "proxy/store/central" ).toURI().toURL().toString() );

                Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
                repoConf.setExternalConfiguration( ex );
                M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );
                exConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
                exConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

                repoConf.setRemoteStorage( new CRemoteStorage() );
                repoConf.getRemoteStorage().setProvider( "apacheHttpClient3x" );
                repoConf.getRemoteStorage().setUrl( "http://whatever.server/foo/but/be/a/valid/url" );

                repo.configure( repoConf );

                abstractProxyTestEnvironment.getApplicationConfiguration().getConfigurationModel().addRepository(
                    repoConf );

                abstractProxyTestEnvironment.getRepositoryRegistry().addRepository( repo );
            }
        };
    }

    public void testNEXUS3064PerRepo()
        throws Exception
    {
        long rscChangeTs = aProxyRepository.getRemoteStorageContext().getLastChanged();

        RemoteProxySettings proxy = aProxyRepository.getRemoteProxySettings();

        assertFalse( "Should no proxy be set!", proxy.isEnabled() );

        proxy.setHostname( "192.168.1.1" );

        proxy.setPort( 1234 );

        // TODO: this is the spurious part!!! Make it not needed! Config framework DOES know it changed!
        // If you remove this, test will fail
        aProxyRepository.setRemoteProxySettings( proxy );

        applicationConfiguration.saveConfiguration();

        assertTrue( "The change should be detected",
            aProxyRepository.getRemoteStorageContext().getLastChanged() > rscChangeTs );
    }

    public void testNEXUS3064Global()
        throws Exception
    {
        long rscChangeTs = aProxyRepository.getRemoteStorageContext().getLastChanged();

        // and the problem now
        // change the global proxy
        RemoteProxySettings proxy =
            getApplicationConfiguration().getGlobalRemoteStorageContext().getRemoteProxySettings();

        proxy.setHostname( "192.168.1.1" );

        proxy.setPort( 1234 );

        applicationConfiguration.saveConfiguration();

        // TODO: this is the spurious part!!! Make it not needed! Config framework DOES know it changed!
        // If you remove this, test will fail
        applicationConfiguration.getGlobalRemoteStorageContext().setRemoteProxySettings( proxy );

        assertTrue( "The change should be detected",
            aProxyRepository.getRemoteStorageContext().getLastChanged() > rscChangeTs );

    }

}
