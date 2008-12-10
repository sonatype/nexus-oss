/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.storage.remote.jetty;

import org.mortbay.jetty.HttpStatus;
import org.mortbay.jetty.client.CachedExchange;
import org.mortbay.util.DateCache;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public abstract class AbstractNexusExchange
    extends CachedExchange
{
    private final RepositoryItemUid uid;

    private final DateCache dateCache;

    public AbstractNexusExchange( RepositoryItemUid uid )
    {
        super( true );

        this.uid = uid;

        this.dateCache = new DateCache();
    }

    protected RepositoryItemUid getRepositoryItemUid()
    {
        return uid;
    }

    protected DateCache getDateCache()
    {
        return dateCache;
    }

    public boolean isSuccesful()
        throws StorageException,
            ItemNotFoundException
    {
        validate();

        return true;
    }

    public void validate()
        throws ItemNotFoundException,
            StorageException
    {
        try
        {
            waitForDone();
        }
        catch ( InterruptedException e )
        {
            throw new StorageException( e );
        }

        if ( getResponseStatus() == HttpStatus.ORDINAL_404_Not_Found )
        {
            throw new ItemNotFoundException( getRepositoryItemUid() );
        }

        if ( !doValidate() )
        {
            throw new StorageException( "Response to '" + toString() + "' exchange was not not expected: "
                + HttpStatus.getResponseLine( getResponseStatus() ) );
        }
    }

    protected abstract boolean doValidate()
        throws ItemNotFoundException,
            StorageException;

}
