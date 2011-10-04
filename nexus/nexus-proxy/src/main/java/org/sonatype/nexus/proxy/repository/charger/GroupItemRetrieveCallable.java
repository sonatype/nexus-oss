/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
