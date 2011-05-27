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

import org.junit.Test;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class ObrMetadataSourceTest
    extends AbstractObrMetadataTest
{

    @Test
    public void testBundleItem1()
        throws Exception
    {
        ContentLocator content = new ContentLocator()
        {
            public InputStream getContent()
                throws IOException
            {
                return getResourceAsStream( "/obr/jars/osgi.core.jar" );
            }

            public boolean isReusable()
            {
                return false;
            }

            public String getMimeType()
            {
                return "application/java-archive";
            }
        };

        StorageFileItem item = createStorageFileItem( "/valid/bundle/1", content );

        Resource resource = obrMetadataSource.buildResource( item );

        assertNotNull( resource );
    }

    @Test
    public void testBundleItem2()
        throws Exception
    {
        ContentLocator content = new ContentLocator()
        {
            public InputStream getContent()
                throws IOException
            {
                return getResourceAsStream( "/obr/jars/org.eclipse.core.runtime_3.4.0.v20080512.jar" );
            }

            public boolean isReusable()
            {
                return false;
            }

            public String getMimeType()
            {
                return "application/java-archive";
            }
        };

        StorageFileItem item = createStorageFileItem( "/valid/bundle/2", content );

        Resource resource = obrMetadataSource.buildResource( item );
        for ( Requirement req : resource.getRequirements() )
        {
            if ( req.getFilter().contains( "org.eclipse.osgi" ) )
            {
                assertFalse( "NXCM-1365: org.eclipse.osgi dependency should not be optional", req.isOptional() );
            }
        }

        assertNotNull( resource );
    }

    @Test
    public void testNonBundleItem()
        throws Exception
    {
        ContentLocator content = new ContentLocator()
        {
            public InputStream getContent()
                throws IOException
            {
                return getResourceAsStream( "/obr/jars/maven-model-2.0.jar" );
            }

            public boolean isReusable()
            {
                return false;
            }

            public String getMimeType()
            {
                return "application/java-archive";
            }
        };

        StorageFileItem item = createStorageFileItem( "/non/bundle", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    @Test
    public void testBrokenStream()
        throws Exception
    {
        ContentLocator content = new ContentLocator()
        {
            public InputStream getContent()
                throws IOException
            {
                throw new IOException( "EOF" );
            }

            public boolean isReusable()
            {
                return false;
            }

            public String getMimeType()
            {
                return "application/java-archive";
            }
        };

        StorageFileItem item = createStorageFileItem( "/broken/stream", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    @Test
    public void testNullStream()
        throws Exception
    {
        ContentLocator content = new ContentLocator()
        {
            public InputStream getContent()
                throws IOException
            {
                return null;
            }

            public boolean isReusable()
            {
                return false;
            }

            public String getMimeType()
            {
                return "application/java-archive";
            }
        };

        StorageFileItem item = createStorageFileItem( "/null/stream", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    private StorageFileItem createStorageFileItem( String path, ContentLocator content )
    {
        return new DefaultStorageFileItem( testRepository, new ResourceStoreRequest( path ), true, true, content );
    }
}
