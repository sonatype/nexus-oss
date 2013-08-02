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
package org.sonatype.nexus.plugins.repository.api;

import javax.inject.Named;
import javax.inject.Singleton;

import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Named
@Singleton
public class StorageLinkedItemPlexusResource
    extends AbstractNexusPlexusResource
{

    public static final String REPOSITORY_ID_KEY = "repositoryId";

    public static final String PATH_KEY = "path";

    public static final String LINK_TO_REPOSITORY_PARAM = "linkToRepository";

    public static final String LINK_TO_PATH_PARAM = "linkToPath";

    public static final String RESOURCE_URI = "/it/storage/link/{" + REPOSITORY_ID_KEY + "}/{" + PATH_KEY + "}";

    public StorageLinkedItemPlexusResource()
    {
        this.setModifiable( true );
        this.setReadable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new Link();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/it/storage/link/*/*", "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public Object put( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final String repoId = getRepositoryId( request );
        final String path = getPath( request );
        final Link link = (Link) payload;
        try
        {
            final Repository repository = getRepositoryRegistry().getRepository( repoId );
            Repository linkedRepository = repository;
            if ( link.getLinkedRepositoryId() != null )
            {
                linkedRepository = getRepositoryRegistry().getRepository( link.getLinkedRepositoryId() );
            }
            try
            {
                repository.retrieveItem( new ResourceStoreRequest( path, true, false ) );
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Item '" + path + "' already exists (use POST to update link)"
                );
            }
            catch ( ItemNotFoundException e )
            {
                final DefaultStorageLinkItem linkItem = new DefaultStorageLinkItem(
                    repository,
                    new ResourceStoreRequest( "/" + path ),
                    true,
                    true,
                    linkedRepository.createUid(
                        ( link.getLinkedPath().startsWith( "/" ) ? "" : "/" ) + link.getLinkedPath()
                    )
                );
                repository.storeItem( false, linkItem );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_NOT_FOUND, "Repository with id '" + repoId + "' does not exist"
            );
        }
        catch ( Exception e )
        {
            throw new ResourceException( e );
        }
        return null;
    }

    @Override
    public Object post( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final String repoId = getRepositoryId( request );
        final String path = getPath( request );
        final Link link = (Link) payload;
        try
        {
            final Repository repository = getRepositoryRegistry().getRepository( repoId );
            Repository linkedRepository = repository;
            if ( link.getLinkedRepositoryId() != null )
            {
                linkedRepository = getRepositoryRegistry().getRepository( link.getLinkedRepositoryId() );
            }
            final StorageItem storageItem = repository.retrieveItem( new ResourceStoreRequest( path, true, false ) );
            if ( !( storageItem instanceof StorageLinkItem ) )
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Item '" + path + "' is not a linked item"
                );
            }
            final DefaultStorageLinkItem linkItem = new DefaultStorageLinkItem(
                repository,
                new ResourceStoreRequest( "/" + path ),
                true,
                true,
                linkedRepository.createUid(
                    ( link.getLinkedPath().startsWith( "/" ) ? "" : "/" ) + link.getLinkedPath()
                )
            );
            repository.storeItem( false, linkItem );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_NOT_FOUND, "Repository with id '" + repoId + "' does not exist"
            );
        }
        catch ( ItemNotFoundException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_NOT_FOUND, "Path '" + path + "' does not exist in repository"
            );
        }
        catch ( Exception e )
        {
            throw new ResourceException( e );
        }
        return null;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        final String repoId = getRepositoryId( request );
        final String path = getPath( request );

        try
        {
            final Repository repository = getRepositoryRegistry().getRepository( repoId );
            final StorageItem storageItem = repository.retrieveItem( new ResourceStoreRequest( path, true, false ) );
            if ( !( storageItem instanceof StorageLinkItem ) )
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, "Item '" + path + "' is not a linked item"
                );
            }
            repository.deleteItem( new ResourceStoreRequest( path ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_NOT_FOUND, "Repository with id '" + repoId + "' does not exist"
            );
        }
        catch ( ItemNotFoundException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_NOT_FOUND, "Path '" + path + "' does not exist in repository"
            );
        }
        catch ( Exception e )
        {
            throw new ResourceException( e );
        }
    }

    private String getRepositoryId( final Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    private String getPath( final Request request )
    {
        return request.getAttributes().get( PATH_KEY ).toString();
    }

    private String getLinkedPath( final Request request )
    {
        final Parameter parameter = request.getResourceRef().getQueryAsForm().getFirst( LINK_TO_PATH_PARAM );
        if ( parameter != null )
        {
            return parameter.getValue();
        }
        return null;
    }

    private String getLinkedRepository( final Request request )
    {
        final Parameter parameter = request.getResourceRef().getQueryAsForm().getFirst( LINK_TO_REPOSITORY_PARAM );
        if ( parameter != null )
        {
            return parameter.getValue();
        }
        return null;
    }

    public static class Link
    {

        private String linkedRepositoryId;

        private String linkedPath;

        public Link()
        {
        }

        public Link( final String linkedRepositoryId,
                     final String linkedPath )
        {
            this.linkedRepositoryId = linkedRepositoryId;
            this.linkedPath = linkedPath;
        }

        public String getLinkedRepositoryId()
        {
            return linkedRepositoryId;
        }

        public void setLinkedRepositoryId( final String linkedRepositoryId )
        {
            this.linkedRepositoryId = linkedRepositoryId;
        }

        public String getLinkedPath()
        {
            return linkedPath;
        }

        public void setLinkedPath( final String linkedPath )
        {
            this.linkedPath = linkedPath;
        }
    }

}
