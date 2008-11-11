package org.sonatype.nexus.proxy.storage.remote.jetty;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpStatus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class ContainsHttpExchange
    extends AbstractNexusExchange
{

    public ContainsHttpExchange( RepositoryItemUid uid, long newerThen )
    {
        super( uid );

        setMethod( HttpMethods.HEAD );

        if ( newerThen > 0 )
        {
            addRequestHeader( HttpHeaders.IF_MODIFIED_SINCE, getDateCache().format( newerThen ) );
        }
    }

    @Override
    protected boolean doValidate()
        throws ItemNotFoundException,
            StorageException
    {
        if ( getResponseStatus() == HttpStatus.ORDINAL_200_OK
            || getResponseStatus() == HttpStatus.ORDINAL_304_Not_Modified )
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

    public boolean isContained()
        throws ItemNotFoundException,
            StorageException
    {
        isSuccesful();

        return HttpStatus.ORDINAL_304_Not_Modified != getResponseStatus();
    }

}
