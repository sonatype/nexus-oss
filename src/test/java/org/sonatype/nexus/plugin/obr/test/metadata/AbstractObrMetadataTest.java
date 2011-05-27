/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.obr.test.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.prefs.Preferences;
import java.util.zip.ZipInputStream;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.sonatype.nexus.test.PlexusTestCaseSupport;
import org.codehaus.plexus.context.Context;
import org.sonatype.licensing.product.ProductLicenseManager;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.obr.metadata.ObrMetadataSource;
import org.sonatype.nexus.obr.metadata.ObrSite;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

import com.sonatype.nexus.licensing.DefaultNexusLicenseBuilder;

public abstract class AbstractObrMetadataTest
    extends PlexusTestCaseSupport
{
    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        super.customizeContainerConfiguration( containerConfiguration );

        containerConfiguration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    private ProductLicenseManager licensingManager;

    protected ObrMetadataSource obrMetadataSource;

    protected Repository testRepository;

    protected NexusConfiguration nexusConfig;

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "nexus-work", getBasedir() + "/target/nexus-work" );
        context.put( "security-xml-file", getBasedir() + "/target/nexus-work/conf/security.xml" );
        context.put( "application-conf", getBasedir() + "/target/nexus-work/conf/" );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        licensingManager = lookup( ProductLicenseManager.class );
        licensingManager.uninstallLicense();

        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).remove( "trialEligible" );

        nexusConfig = lookup( NexusConfiguration.class );

        obrMetadataSource = lookup( ObrMetadataSource.class, "obr-bindex" );

        testRepository = lookup( Repository.class, "maven2" );

        nexusConfig.loadConfiguration();

        CRepository crepo = new DefaultCRepository();
        crepo.setId( "test-repository" );
        crepo.setName( "test-repository" );
        CLocalStorage clocal = new CLocalStorage();
        clocal.setUrl( getBasedir() + "/target/test-classes" );
        clocal.setProvider( "file" );
        crepo.setLocalStorage( clocal );
        testRepository.configure( crepo );
    }

    protected ObrSite openObrSite( RepositoryItemUid uid )
        throws StorageException, ItemNotFoundException
    {
        return openObrSite( uid.getRepository(), uid.getPath() );
    }

    protected ObrSite openObrSite( Repository repository, final String path )
        throws StorageException, ItemNotFoundException
    {
        ResourceStoreRequest request = new ResourceStoreRequest( path );

        final URL url = repository.getLocalStorage().getAbsoluteUrlFromBase( repository, request );

        return new ObrSite()
        {
            public String getMetadataPath()
            {
                return path;
            }

            public URL getMetadataUrl()
            {
                return url;
            }

            public InputStream openStream()
                throws IOException
            {
                URLConnection conn = url.openConnection();

                if ( "application/zip".equalsIgnoreCase( conn.getContentType() ) )
                {
                    // assume metadata is the first entry in the zipfile
                    ZipInputStream zis = new ZipInputStream( conn.getInputStream() );
                    zis.getNextEntry();
                    return zis;
                }

                return conn.getInputStream();
            }
        };
    }
}
