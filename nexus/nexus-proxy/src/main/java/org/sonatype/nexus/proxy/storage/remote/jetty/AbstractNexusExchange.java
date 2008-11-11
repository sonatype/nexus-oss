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
