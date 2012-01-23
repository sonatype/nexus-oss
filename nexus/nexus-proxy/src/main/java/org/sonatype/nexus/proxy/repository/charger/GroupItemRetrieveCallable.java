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
package org.sonatype.nexus.proxy.repository.charger;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.sisu.charger.ExceptionHandler;

import com.google.common.base.Preconditions;

/**
 * Callable that retrieves an item from a repository, as part of a group request. Used in
 * org.sonatype.nexus.proxy.repository.AbstractGroupRepository.doRetrieveItem(ResourceStoreRequest). All exceptions are
 * supressed by this same class (it implements ExceptionHandler).
 * 
 * @author cstamas
 */
public class GroupItemRetrieveCallable
implements Callable<StorageItem>, ExceptionHandler
{
    private final Logger logger;

    private final Repository repository;

    private final ResourceStoreRequest request;

    private final GroupRepository groupRepository;

    public GroupItemRetrieveCallable( final Logger logger, final Repository repository,
                                      final ResourceStoreRequest request, final GroupRepository groupRepository )
    {
        this.logger = Preconditions.checkNotNull( logger );
        this.repository = Preconditions.checkNotNull( repository );
        this.request = Preconditions.checkNotNull( request );
        this.groupRepository = Preconditions.checkNotNull( groupRepository );
    }

    @Override
    public StorageItem call()
        throws ItemNotFoundException, IllegalOperationException, IOException, AccessDeniedException
    {
        final ResourceStoreRequest newreq = new ResourceStoreRequest( request );
        newreq.setRequestLocalOnly( request.isRequestLocalOnly() );
        newreq.setRequestRemoteOnly( request.isRequestRemoteOnly() );

        StorageItem item = repository.retrieveItem( newreq );

        request.addProcessedRepository( repository );

        if ( item instanceof StorageCollectionItem )
        {
            item = new DefaultStorageCollectionItem( groupRepository, newreq, true, false );
        }

        return item;
    }

    @Override
    public boolean handle( Exception ex )
    {
        if ( ex instanceof IOException )
        {
            // just ignore it
            return true;
        }
        else if ( ex instanceof AccessDeniedException )
        {
            // just ignore it
            return true;
        }
        else if ( ex instanceof ItemNotFoundException )
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
