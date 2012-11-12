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
package org.sonatype.nexus.proxy;

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

    private final ResourceStoreRequest request;

    private final Repository repository;

    /**
     * Do not use this constructor!
     * 
     * @param path
     * @deprecated use a constructor that accepts a request!
     */
    @Deprecated
    public ItemNotFoundException( String path )
    {
        this( path, null );
    }

    /**
     * Do not use this constructor!
     * 
     * @param path
     * @param cause
     * @deprecated use a constructor that accepts a request!
     */
    @Deprecated
    public ItemNotFoundException( String path, Throwable cause )
    {
        super( "Item not found on path " + path, cause );
        this.repository = null;
        this.request = null;
    }

    /**
     * Constructor. To be used in places where no Repository exists yet in context (like in a Router).
     * 
     * @param request
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
     */
    protected ItemNotFoundException( final String message, final ResourceStoreRequest request,
                                     final Repository repository, final Throwable cause )
    {
        super( message, cause );
        this.request = request;
        this.repository = repository;
    }

    // ==

    public Repository getRepository()
    {
        return repository;
    }

    public ResourceStoreRequest getRequest()
    {
        return request;
    }
}
