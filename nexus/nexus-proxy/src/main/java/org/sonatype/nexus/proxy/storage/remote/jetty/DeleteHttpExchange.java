package org.sonatype.nexus.proxy.storage.remote.jetty;

import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpStatus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class DeleteHttpExchange
    extends AbstractNexusExchange
{
    public DeleteHttpExchange( RepositoryItemUid uid )
    {
        super( uid );

        setMethod( HttpMethods.DELETE );
    }

    @Override
    protected boolean doValidate()
        throws ItemNotFoundException,
            StorageException
    {
        if ( getResponseStatus() == HttpStatus.ORDINAL_200_OK || getResponseStatus() == HttpStatus.ORDINAL_202_Accepted
            || getResponseStatus() == HttpStatus.ORDINAL_204_No_Content )
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
}
