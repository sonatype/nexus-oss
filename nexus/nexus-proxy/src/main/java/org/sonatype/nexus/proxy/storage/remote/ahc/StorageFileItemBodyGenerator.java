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
package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.sonatype.nexus.proxy.item.StorageFileItem;

import com.ning.http.client.Body;
import com.ning.http.client.BodyGenerator;

/**
 * A BodyGenerator that uses StorageFileItem as it's content source.
 * 
 * @author cstamas
 */
public class StorageFileItemBodyGenerator
    implements BodyGenerator
{
    private final StorageFileItem file;

    public StorageFileItemBodyGenerator( StorageFileItem file )
    {
        this.file = file;
    }

    @Override
    public Body createBody()
        throws IOException
    {
        return new StorageFileItemBody( file );
    }

    // ==

    public static class StorageFileItemBody
        implements Body
    {
        private final StorageFileItem file;

        private final ReadableByteChannel channel;

        public StorageFileItemBody( StorageFileItem file )
            throws IOException
        {
            this.file = file;

            this.channel = Channels.newChannel( file.getInputStream() );
        }

        @Override
        public void close()
            throws IOException
        {
            channel.close();
        }

        @Override
        public long getContentLength()
        {
            return file.getLength();
        }

        @Override
        public long read( ByteBuffer buffer )
            throws IOException
        {
            return channel.read( buffer );
        }
    }
}
