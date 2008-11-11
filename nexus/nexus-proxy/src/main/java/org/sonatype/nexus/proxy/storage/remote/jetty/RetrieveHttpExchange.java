package org.sonatype.nexus.proxy.storage.remote.jetty;

import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpStatus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class RetrieveHttpExchange
    extends AbstractNexusExchange
{
    public RetrieveHttpExchange( RepositoryItemUid uid )
    {
        super( uid );

        setMethod( HttpMethods.GET );
    }

    @Override
    protected boolean doValidate()
        throws ItemNotFoundException,
            StorageException
    {
        if ( getResponseStatus() == HttpStatus.ORDINAL_200_OK )
        {
            // ok
            return true;
        }
        else
        {
            // unexpected
            return false;
        }
    }

    public AbstractStorageItem getStorageItem()
    {
        return null;
    }

}
