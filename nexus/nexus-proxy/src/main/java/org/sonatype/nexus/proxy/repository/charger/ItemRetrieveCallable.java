package org.sonatype.nexus.proxy.repository.charger;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

import com.google.common.base.Preconditions;

/**
 * Callable that retrieves an item from a repository, for example as part of a group request. Used in
 * org.sonatype.nexus.proxy.repository.AbstractGroupRepository.doRetrieveItems(ResourceStoreRequest)
 * 
 * @author cstamas
 */
public class ItemRetrieveCallable
    implements Callable<StorageItem>, ExceptionHandler
{
    private final Logger logger;

    private final Repository repository;

    private final ResourceStoreRequest request;

    public ItemRetrieveCallable( final Logger logger, final Repository repository, final ResourceStoreRequest request )
    {
        this.logger = Preconditions.checkNotNull( logger );
        this.repository = Preconditions.checkNotNull( repository );
        this.request = Preconditions.checkNotNull( request );
    }

    @Override
    public StorageItem call()
        throws IllegalOperationException, ItemNotFoundException, IOException
    {
        final ResourceStoreRequest newreq = new ResourceStoreRequest( request );
        newreq.setRequestLocalOnly( request.isRequestLocalOnly() );
        newreq.setRequestRemoteOnly( request.isRequestRemoteOnly() );

        try
        {
            return repository.retrieveItem( false, newreq );
        }
        finally
        {
            request.addProcessedRepository( repository );
        }
    }

    @Override
    public boolean handle( Exception ex )
    {
        if ( ex instanceof ItemNotFoundException )
        {
            // just ignore it
            return true;
        }
        else if ( ex instanceof RepositoryNotAvailableException )
        {
            // just log and ignore it
            if ( logger.isDebugEnabled() )
            {
                logger.debug( RepositoryStringUtils.getFormattedMessage(
                    "Member repository %s is not available, request failed.",
                    ( (RepositoryNotAvailableException) ex ).getRepository() ) );
            }

            return true;
        }
        else if ( ex instanceof IllegalOperationException )
        {
            // just log and ignore it
            logger.warn( "Member repository request failed", ex );

            return true;
        }

        return false;
    }
}
