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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.util.WrappingInputStream;

/**
 * This is a simple wrapper implementation of ContentLocator, that wraps any other ContentLocator, while doing proper
 * {@link Action} read locking against the UID the locator points to.
 * 
 * @author cstamas
 */
public class ReadLockingContentLocator
    implements ContentLocator
{
    private final RepositoryItemUid wrappedUid;

    private final ContentLocator wrappedLocator;

    public ReadLockingContentLocator( final RepositoryItemUid wrappedUid, final ContentLocator wrappedLocator )
    {
        this.wrappedUid = wrappedUid;

        this.wrappedLocator = wrappedLocator;
    }

    public InputStream getContent()
        throws IOException
    {
        final RepositoryItemUidLock lock = wrappedUid.createLock();

        lock.lock( Action.read );

        try
        {
            return new ReadLockingInputStream( lock, wrappedLocator.getContent() );
        }
        catch ( IOException e )
        {
            lock.unlock();
            lock.release();
            
            throw e;
        }
        catch ( Exception e )
        {
            lock.unlock();
            lock.release();
            
            // wrap it
            IOException w = new IOException( e.getMessage() );
            w.initCause( e );
            throw w;
        }
    }

    public String getMimeType()
    {
        return wrappedLocator.getMimeType();
    }

    public boolean isReusable()
    {
        return wrappedLocator.isReusable();
    }

    // ==

    private class ReadLockingInputStream
        extends WrappingInputStream
    {
        private volatile RepositoryItemUidLock lock;

        public ReadLockingInputStream( final RepositoryItemUidLock lock, final InputStream wrappedStream )
        {
            super( wrappedStream );

            this.lock = lock;
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                super.close();
            }
            finally
            {
                if ( lock != null )
                {
                    lock.unlock();
                    lock.release();

                    lock = null;
                }
            }
        }
    }
}
