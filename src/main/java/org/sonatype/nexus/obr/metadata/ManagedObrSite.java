/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

/**
 * An {@link ObrSite} that's managed by Nexus.
 */
public class ManagedObrSite
    extends AbstractObrSite
{
    private final StorageFileItem item;

    private final URL url;

    /**
     * Creates a managed OBR site based on the given metadata item inside Nexus.
     * 
     * @param item the metadata item
     * @throws StorageException
     */
    public ManagedObrSite( final StorageFileItem item )
        throws StorageException
    {
        this.item = item;

        url = getAbsoluteUrlFromBase( item );
    }

    /**
     * Finds the absolute URL for the managed OBR site.
     * 
     * @return the absolute URL
     * @throws StorageException
     */
    private static URL getAbsoluteUrlFromBase( final StorageFileItem item )
        throws StorageException
    {
        final RepositoryItemUid uid = item.getRepositoryItemUid();

        final Repository repository = uid.getRepository();
        final ResourceStoreRequest request = new ResourceStoreRequest( uid.getPath() );

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            final ProxyRepository proxyRepository = repository.adaptToFacet( ProxyRepository.class );
            final RemoteRepositoryStorage storage = proxyRepository.getRemoteStorage();
            if ( storage != null )
            {
                return storage.getAbsoluteUrlFromBase( proxyRepository, request );
            }
            // locally hosted proxy repository, so drop through...
        }

        return repository.getLocalStorage().getAbsoluteUrlFromBase( repository, request );
    }

    public URL getMetadataUrl()
    {
        return url;
    }

    public String getMetadataPath()
    {
        return item.getRepositoryItemUid().getPath();
    }

    @Override
    protected InputStream openRawStream()
        throws IOException
    {
        return item.getInputStream();
    }

    @Override
    protected String getContentType()
    {
        return item.getMimeType();
    }
}
