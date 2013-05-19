/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository.charger;

import java.io.IOException;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.sisu.charger.ExceptionHandler;

/**
 * Callable that retrieves an item from a repository, for example as part of a group request. Used in
 * org.sonatype.nexus.proxy.repository.AbstractGroupRepository.doRetrieveItems(ResourceStoreRequest)
 * 
 * @author cstamas
 */
public class ItemRetrieveCallable
   extends AbstractRetrieveCallable<StorageItem>
    implements ExceptionHandler
{
    public ItemRetrieveCallable( final Logger logger, final Repository repository, final ResourceStoreRequest request )
    {
        super(logger, repository ,request);
    }

    @Override
    public StorageItem call()
        throws IllegalOperationException, ItemNotFoundException, IOException
    {
        final ResourceStoreRequest newreq = new ResourceStoreRequest( getRequest() );
        newreq.setRequestLocalOnly( getRequest().isRequestLocalOnly() );
        newreq.setRequestRemoteOnly( getRequest().isRequestRemoteOnly() );

        try
        {
            return getRepository().retrieveItem( false, newreq );
        }
        finally
        {
            getRequest().addProcessedRepository( getRepository() );
        }
    }

    @Override
    public boolean handle( Exception ex )
    {
        if ( ex instanceof ItemNotFoundException )
        {
            // just ignore it
            setProcessingException( ex );
            return true;
        }
        else if ( ex instanceof RepositoryNotAvailableException )
        {
            // just log and ignore it
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( RepositoryStringUtils.getFormattedMessage(
                    "Member repository %s is not available, request failed.",
                    ( (RepositoryNotAvailableException) ex ).getRepository() ) );
            }

            setProcessingException( ex );
            return true;
        }
        else if ( ex instanceof IllegalOperationException )
        {
            // just log and ignore it
            getLogger().warn( "Member repository request failed", ex );

            setProcessingException( ex );
            return true;
        }

        return false;
    }
}
