/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySource;

/**
 * {@link WritableEntrySource} implementation that is backed by a {@link StorageFileItem} in a {@link MavenRepository}.
 * Also serves as "the main" WL source. This is the only implementation of the {@link WritableEntrySource}.
 * 
 * @author cstamas
 * @since 2.4
 */
public class FileEntrySource
    extends AbstractFileEntrySource
    implements WritableEntrySource
{
    /**
     * Constructor.
     * 
     * @param mavenRepository
     * @param path
     */
    public FileEntrySource( final MavenRepository mavenRepository, final String path )
    {
        super( mavenRepository, path, new PrefixesFileMarshaller() );
    }

    @Override
    public void writeEntries( final EntrySource entrySource )
        throws IOException
    {
        if ( entrySource instanceof FileEntrySource && equals( (FileEntrySource) entrySource ) )
        {
            // we would read and then write to the same file, don't do it
            return;
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getEntrySourceMarshaller().write( entrySource, bos );
        putFileItem( new PreparedContentLocator( new ByteArrayInputStream( bos.toByteArray() ), "text/plain" ) );
    }

    @Override
    public void delete()
        throws IOException
    {
        deleteFileItem();
    }
}
