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
package org.sonatype.nexus.proxy;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.proxy.ItemNotFoundException.ItemNotFoundInRepositoryReason;
import org.sonatype.nexus.proxy.ItemNotFoundException.ItemNotFoundReason;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.sisu.goodies.common.FormatTemplate;
import org.sonatype.sisu.goodies.common.SimpleFormat;

/**
 * Class with some handy static methods to contruct reasoning for {@link ItemNotFoundException}.
 * 
 * @author cstamas
 * @since 2.4
 */
public class ItemNotFoundReasons
{
    private static class ItemNotFoundReasonImpl
        implements ItemNotFoundReason
    {
        private final FormatTemplate message;

        private final ResourceStoreRequest resourceStoreRequest;

        public ItemNotFoundReasonImpl( final FormatTemplate message, final ResourceStoreRequest resourceStoreRequest )
        {
            this.message = checkNotNull( message );
            this.resourceStoreRequest = checkNotNull( resourceStoreRequest );
        }

        @Override
        public String getMessage()
        {
            return message.toString();
        }

        @Override
        public ResourceStoreRequest getResourceStoreRequest()
        {
            return resourceStoreRequest;
        }
    }

    private static class ItemNotFoundInRepositoryReasonImpl
        extends ItemNotFoundReasonImpl
        implements ItemNotFoundInRepositoryReason
    {
        private final Repository repository;

        public ItemNotFoundInRepositoryReasonImpl( final FormatTemplate message,
                                                   final ResourceStoreRequest resourceStoreRequest,
                                                   final Repository repository )
        {
            super( message, resourceStoreRequest );
            this.repository = checkNotNull( repository );
        }

        @Override
        public Repository getRepository()
        {
            return repository;
        }
    }

    // ==

    /**
     * Creates a new instance of {@link ItemNotFoundReason}.
     * 
     * @param request
     * @param message
     * @param params
     * @return the newly created reason.
     */
    public static ItemNotFoundReason reasonFor( final ResourceStoreRequest request, final String message,
                                                final Object... params )
    {
        return new ItemNotFoundReasonImpl( SimpleFormat.template( message, params ), request );
    }

    /**
     * Creates a new instance of {@link ItemNotFoundInRepositoryReason}.
     * 
     * @param request
     * @param repository
     * @param message
     * @param params
     * @return the newly created reason.
     */
    public static ItemNotFoundInRepositoryReason reasonFor( final ResourceStoreRequest request,
                                                            final Repository repository, final String message,
                                                            final Object... params )
    {
        return new ItemNotFoundInRepositoryReasonImpl( SimpleFormat.template( message, params ), request, repository );
    }

    /**
     * Looks for existing {@link ItemNotFoundInRepositoryReason} in passed in {@link ResourceStoreRequest}, and if
     * present returns that. If not present, will construct one with passed in message and params.
     * 
     * @param request
     * @param repository
     * @param message
     * @param params
     * @return reason existed in request or new one.
     */
    public static ItemNotFoundInRepositoryReason checkReasonFrom( final ResourceStoreRequest request,
                                                                  final Repository repository, final String message,
                                                                  final Object... params )
    {
        final ItemNotFoundInRepositoryReason reason = request.getLastItemNotFoundReason();
        if ( reason != null )
        {
            return reason;
        }
        else
        {
            // no reason found, construct one
            return reasonFor( request, repository, message, params );
        }
    }

    /**
     * Looks for existing {@link ItemNotFoundInRepositoryReason} in passed in {@link ResourceStoreRequest}, and if
     * present returns that. If not present, will construct one with defaulted message
     * {@code "Path %s not found in repository %s."}.
     * 
     * @param request
     * @param repository
     * @return reason existed in request or a new one with default message.
     */
    public static ItemNotFoundInRepositoryReason checkReasonFrom( final ResourceStoreRequest request,
                                                                  final Repository repository )
    {
        // default the message
        return checkReasonFrom( request, repository, "Path %s not found in repository %s.", request.getRequestPath(),
            RepositoryStringUtils.getHumanizedNameString( repository ) );
    }

    // ==

    /**
     * Legacy support.
     * 
     * @param message
     * @param request
     * @param repository
     * @return reason.
     * @deprecated Used for legacy support, new code should NOT use this method. See other methods:
     *             {@link #reasonFor(ResourceStoreRequest, String, Object...)} and
     *             {@link #reasonFor(ResourceStoreRequest, Repository, String, Object...)}
     */
    public static ItemNotFoundReason legacySupport( final String message, final ResourceStoreRequest request,
                                                    final Repository repository )
    {
        if ( repository != null )
        {
            return new ItemNotFoundInRepositoryReasonImpl( SimpleFormat.template( message ), request, repository );
        }
        else
        {
            return new ItemNotFoundReasonImpl( SimpleFormat.template( message ), request );
        }
    }
}
