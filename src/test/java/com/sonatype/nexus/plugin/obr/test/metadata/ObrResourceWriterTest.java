/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.obr.test.metadata;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.codehaus.plexus.util.IOUtil;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

import com.sonatype.nexus.obr.metadata.ObrResourceReader;
import com.sonatype.nexus.obr.metadata.ObrResourceWriter;
import com.sonatype.nexus.obr.metadata.ObrSite;

public class ObrResourceWriterTest
    extends AbstractObrMetadataTest
{

    @Test
    public void testObrSerializing()
        throws Exception
    {
        ObrSite testSite = openObrSite( testRepository, "/obr/samples/osgi_alliance_obr.zip" );

        RepositoryItemUid uid = testRepository.createUid( "/obr/repository.xml" );

        ObrResourceReader reader = obrMetadataSource.getReader( testSite );
        ObrResourceWriter writer = obrMetadataSource.getWriter( uid );

        Collection<Resource> bundles = new ArrayList<Resource>();

        Resource r;
        while ( ( r = reader.readResource() ) != null )
        {
            bundles.add( r );
            writer.append( r );
            writer.flush();
        }

        assertEquals( 2710, bundles.size() );

        writer.complete();
        writer.close();

        reader = obrMetadataSource.getReader( openObrSite( uid ) );
        while ( ( r = reader.readResource() ) != null )
        {
            assertTrue( bundles.remove( r ) );
        }

        assertEquals( Collections.emptyList(), bundles );
    }

    @Test
    public void testRoundTripping()
        throws Exception
    {
        RepositoryItemUid uid1 = testRepository.createUid( "/obr/samples/sample.xml" );
        RepositoryItemUid uid2 = testRepository.createUid( "/obr/sample.xml" );
        RepositoryItemUid uid3 = testRepository.createUid( "/sample.xml" );

        ObrResourceReader reader = obrMetadataSource.getReader( openObrSite( uid1 ) );
        ObrResourceWriter writer = obrMetadataSource.getWriter( uid2 );

        Collection<URL> urls = new ArrayList<URL>();

        Resource r;
        while ( ( r = reader.readResource() ) != null )
        {
            urls.add( r.getURL() );
            writer.append( r );
            writer.flush();
        }

        reader.close();
        writer.complete();
        writer.close();

        reader = obrMetadataSource.getReader( openObrSite( uid2 ) );
        writer = obrMetadataSource.getWriter( uid3 );

        while ( ( r = reader.readResource() ) != null )
        {
            writer.append( r );
        }

        reader.close();
        writer.complete();
        writer.close();

        reader = obrMetadataSource.getReader( openObrSite( uid3 ) );

        while ( ( r = reader.readResource() ) != null )
        {
            urls.remove( r.getURL() );
        }

        reader.close();

        assertEquals( Collections.emptyList(), urls );
    }

    // NXCM-1360
    public void testRoundTrippingWithLongPackageNames()
        throws Exception
    {
        RepositoryItemUid uid1 = testRepository.createUid( "/obr/samples/long-package-name.xml" );
        RepositoryItemUid uid2 = testRepository.createUid( "/obr/samples/long-package-name.xml.clone" );
        RepositoryItemUid temp;

        for ( int i = 0; i < 5; i++ )
        {
            ObrResourceReader reader = obrMetadataSource.getReader( openObrSite( uid1 ) );
            ObrResourceWriter writer = obrMetadataSource.getWriter( uid2 );

            Resource r;
            while ( ( r = reader.readResource() ) != null )
            {
                writer.append( r );
                writer.flush();
            }

            reader.close();
            writer.complete();
            writer.close();

            if ( i > 0 )
            {
                assertEquals( obrToString( uid1 ), obrToString( uid2 ) );
            }

            temp = uid1;
            uid1 = uid2;
            uid2 = temp;
        }
    }

    private String obrToString( RepositoryItemUid uid )
        throws Exception
    {
        InputStream is = openObrSite( uid ).openStream();
        try
        {
            return IOUtil.toString( is ).replaceFirst( "lastmodified='[0-9]*'", "" );
        }
        finally
        {
            IOUtil.close( is );
        }
    }
}
