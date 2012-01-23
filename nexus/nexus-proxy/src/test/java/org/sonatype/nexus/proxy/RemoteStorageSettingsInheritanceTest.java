/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;
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
                repoConf.getRemoteStorage().setProvider( abstractProxyTestEnvironment.getRemoteProviderHintFactory().getDefaultHttpRoleHint() );
                repoConf.getRemoteStorage().setUrl( "http://whatever.server/foo/but/be/a/valid/url" );

                repo.configure( repoConf );

                abstractProxyTestEnvironment.getApplicationConfiguration().getConfigurationModel().addRepository(
                    repoConf );

                abstractProxyTestEnvironment.getRepositoryRegistry().addRepository( repo );
            }
        };
    }

    @Test
    public void testNEXUS3064PerRepo()
        throws Exception
    {
        long rscChangeTs = aProxyRepository.getRemoteStorageContext().getLastChanged();
        try
        {
            Thread.sleep( 2 );
        }
        catch ( InterruptedException ignore )
        {
        }

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

    @Test
    public void testNEXUS3064Global()
        throws Exception
    {
        long rscChangeTs = aProxyRepository.getRemoteStorageContext().getLastChanged();
        try
        {
            Thread.sleep( 2 );
        }
        catch ( InterruptedException ignore )
        {
        }

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
