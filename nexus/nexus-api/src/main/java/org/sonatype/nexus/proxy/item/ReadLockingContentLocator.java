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

    public ReadLockingContentLocator( RepositoryItemUid wrappedUid, ContentLocator wrappedLocator )
    {
        this.wrappedUid = wrappedUid;

        this.wrappedLocator = wrappedLocator;
    }

    public InputStream getContent()
        throws IOException
    {
        wrappedUid.lock( Action.read );

        try
        {
            return new ReadLockingInputStream( wrappedUid, wrappedLocator.getContent() );
        }
        catch ( IOException e )
        {
            wrappedUid.unlock();

            throw e;
        }
        catch ( Exception e )
        {
            wrappedUid.unlock();

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
        private volatile RepositoryItemUid wrappedUid;

        public ReadLockingInputStream( RepositoryItemUid wrappedUid, InputStream wrappedStream )
        {
            super( wrappedStream );

            this.wrappedUid = wrappedUid;
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
                if ( wrappedUid != null )
                {
                    wrappedUid.unlock();

                    wrappedUid = null;
                }
            }
        }
    }
}
