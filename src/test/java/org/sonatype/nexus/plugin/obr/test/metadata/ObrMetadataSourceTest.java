/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
        final ContentLocator content = new ContentLocator()
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

        final StorageFileItem item = createStorageFileItem( "/valid/bundle/1", content );

        final Resource resource = obrMetadataSource.buildResource( item );

        assertNotNull( resource );
    }

    @Test
    public void testBundleItem2()
        throws Exception
    {
        final ContentLocator content = new ContentLocator()
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

        final StorageFileItem item = createStorageFileItem( "/valid/bundle/2", content );

        final Resource resource = obrMetadataSource.buildResource( item );
        for ( final Requirement req : resource.getRequirements() )
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
        final ContentLocator content = new ContentLocator()
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

        final StorageFileItem item = createStorageFileItem( "/non/bundle", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    @Test
    public void testBrokenStream()
        throws Exception
    {
        final ContentLocator content = new ContentLocator()
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

        final StorageFileItem item = createStorageFileItem( "/broken/stream", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    @Test
    public void testNullStream()
        throws Exception
    {
        final ContentLocator content = new ContentLocator()
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

        final StorageFileItem item = createStorageFileItem( "/null/stream", content );

        assertNull( obrMetadataSource.buildResource( item ) );
    }

    private StorageFileItem createStorageFileItem( final String path, final ContentLocator content )
    {
        return new DefaultStorageFileItem( testRepository, new ResourceStoreRequest( path ), true, true, content );
    }
}
