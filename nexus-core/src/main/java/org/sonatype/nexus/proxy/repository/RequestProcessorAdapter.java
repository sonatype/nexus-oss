/*
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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper that adapts deprecated {@link RequestProcessor} to new {@link RequestProcessor2} API.
 * 
 * @author cstamas
 * @since 2.5
 */
public class RequestProcessorAdapter
    implements RequestProcessor2
{
    private final RequestProcessor requestProcessor;

    public RequestProcessorAdapter( final RequestProcessor requestProcessor )
    {
        this.requestProcessor = checkNotNull( requestProcessor );
    }

    public RequestProcessor getWrappedRequestProcessor()
    {
        return requestProcessor;
    }

    @Override
    public void onHandle( final Repository repository, final ResourceStoreRequest request, final Action action )
        throws ItemNotFoundException
    {
        if ( !requestProcessor.process( repository, request, action ) )
        {
            throw new ItemNotFoundException( ItemNotFoundException.reasonFor( request, repository,
                "Request processing prevented by RequestProcessor %s", requestProcessor.getClass().getName() ) );
        }
    }

    @Override
    public void onServing( final Repository repository, final ResourceStoreRequest request, final StorageItem item )
        throws ItemNotFoundException, IllegalOperationException
    {
        try
        {
            if ( !requestProcessor.shouldRetrieve( repository, request, item ) )
            {
                throw new ItemNotFoundException( ItemNotFoundException.reasonFor( request, repository,
                    "Retrieval prevented by RequestProcessor %s", requestProcessor.getClass().getName() ) );
            }
        }
        catch ( AccessDeniedException e )
        {
            throw new ItemNotFoundException(
                ItemNotFoundException.reasonFor( request, repository, "Retrieval prevented by RequestProcessor %s: %s",
                    requestProcessor.getClass().getName(), e.getMessage() ) );
        }
    }

    @Override
    public void onRemoteAccess( final ProxyRepository repository, final ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        if ( !requestProcessor.shouldProxy( repository, request ) )
        {
            throw new ItemNotFoundException( ItemNotFoundException.reasonFor( request, repository,
                "Proxying prevented by RequestProcessor %s", requestProcessor.getClass().getName() ) );
        }
    }
}
