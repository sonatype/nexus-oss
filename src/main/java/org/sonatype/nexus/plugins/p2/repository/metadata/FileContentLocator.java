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
package org.sonatype.nexus.plugins.p2.repository.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.item.ContentLocator;

import com.google.common.base.Preconditions;

/**
 * A content locator that is backed by a file. Used as "tmp" storage for metadata manipulation that would otherwise eat
 * up a LOT of heap.
 * 
 * @author cstamas
 * @since 2.2
 */
public class FileContentLocator
    implements ContentLocator
{
    private final File file;

    private final String mimeType;

    public FileContentLocator( final String mimeType )
        throws IOException
    {
        this( File.createTempFile( "p2-tmp-content-locator", "tmp" ), mimeType );
    }

    public FileContentLocator( final File file, final String mimeType )
    {
        this.file = Preconditions.checkNotNull( file );
        this.mimeType = Preconditions.checkNotNull( mimeType );
    }

    public InputStream getInputStream()
        throws IOException
    {
        return new FileInputStream( getFile() );
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return new FileOutputStream( getFile() );
    }

    public long getLength()
    {
        return getFile().length();
    }

    public File getFile()
    {
        return file;
    }

    public void delete()
        throws IOException
    {
        FileUtils.forceDelete( getFile() );
    }

    // ==

    @Override
    public InputStream getContent()
        throws IOException
    {
        return getInputStream();
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return true;
    }
}
