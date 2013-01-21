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

import org.sonatype.nexus.proxy.ItemNotFoundReason.ItemNotFoundInRepositoryReason;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

/**
 * Thrown if the requested item is not found.
 * 
 * @author cstamas
 */
public class ItemNotFoundException
    extends Exception
{
    private static final long serialVersionUID = -4964273361722823796L;

    private final ItemNotFoundReason reason;

    /**
     * Constructor.
     * 
     * @param reason
     * @since 2.4
     */
    public ItemNotFoundException( final ItemNotFoundReason reason )
    {
        this( reason, null );
    }

    /**
     * Constructor with cause.
     * 
     * @param reason
     * @param cause
     * @since 2.4
     */
    public ItemNotFoundException( final ItemNotFoundReason reason, final Throwable cause )
    {
        super( reason.getMessage(), cause ); // will NPE with null reason
        this.reason = reason;
    }

    /**
     * Returns the reason of the item not found exception.
     * 
     * @return the reason.
     * @since 2.4
     */
    public ItemNotFoundReason getReason()
    {
        return reason;
    }

    // == Deprecated stuff below

    /**
     * Constructor. To be used in places where no Repository exists yet in context (like in a Router).
     * 
     * @param request
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    public ItemNotFoundException( final ResourceStoreRequest request )
    {
        this( request, null, null );
    }

    /**
     * Constructor. To be used in places where no Repository exists yet in context (like in a Router).
     * 
     * @param request
     * @param cause
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    public ItemNotFoundException( final ResourceStoreRequest request, final Throwable cause )
    {
        this( request, null, cause );
    }

    /**
     * Constructor. To be used in places whenever there IS a Repository in context.
     * 
     * @param request
     * @param repository
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    public ItemNotFoundException( final ResourceStoreRequest request, final Repository repository )
    {
        this( request, repository, null );
    }

    /**
     * Constructor. To be used in places whenever there IS a Repository in context.
     * 
     * @param request
     * @param repository
     * @param cause
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    public ItemNotFoundException( final ResourceStoreRequest request, final Repository repository, final Throwable cause )
    {
        this( repository != null ? "Item not found for request \"" + String.valueOf( request ) + "\" in repository \""
            + RepositoryStringUtils.getHumanizedNameString( repository ) + "\"!" : "Item not found for request \""
            + String.valueOf( request ) + "\"!", request, repository, cause );
    }

    // ==

    /**
     * Protected constructor, to be used by this class and subclass constructors.
     * 
     * @param message
     * @param request
     * @param repository
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    protected ItemNotFoundException( final String message, final ResourceStoreRequest request,
                                     final Repository repository )
    {
        this( message, request, repository, null );
    }

    /**
     * Protected constructor, to be used by this class and subclass constructors.
     * 
     * @param message
     * @param request
     * @param repository
     * @param cause
     * @deprecated Use constructor with {@link ItemNotFoundReason} instead.
     */
    protected ItemNotFoundException( final String message, final ResourceStoreRequest request,
                                     final Repository repository, final Throwable cause )
    {
        this( ItemNotFoundReasons.legacySupport( message, request, repository ), cause );
    }

    // ==

    /**
     * Returns the repository.
     * 
     * @return the repository where this exception occurred or {@code null}.
     * @deprecated Use {@link #getReason()} and inspect that instead.
     */
    public Repository getRepository()
    {
        if ( reason instanceof ItemNotFoundInRepositoryReason )
        {
            return ( (ItemNotFoundInRepositoryReason) reason ).getRepository();
        }
        return null;
    }

    /**
     * The request.
     * 
     * @return the request that caused this exception.
     * @deprecated Use {@link #getReason()} and inspect that instead.
     */
    public ResourceStoreRequest getRequest()
    {
        return getReason().getResourceStoreRequest();
    }
}
