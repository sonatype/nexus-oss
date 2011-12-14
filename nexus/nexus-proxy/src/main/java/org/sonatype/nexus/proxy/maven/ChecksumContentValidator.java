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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Maven checksum content validator.
 * 
 * @author cstamas
 */
@Component( role = ItemContentValidator.class, hint = "ChecksumContentValidator" )
public class ChecksumContentValidator
    extends AbstractChecksumContentValidator
    implements ItemContentValidator
{

    @Override
    protected void cleanup( ProxyRepository proxy, RemoteHashResponse remoteHash, boolean contentValid )
        throws LocalStorageException
    {
        if ( !contentValid && remoteHash.getHashItem() != null )
        {
            // TODO should we remove bad checksum if policy==WARN?
            try
            {
                proxy.getLocalStorage().deleteItem( proxy,
                    new ResourceStoreRequest( remoteHash.getHashItem().getRepositoryItemUid().getPath(), true ) );
            }
            catch ( ItemNotFoundException e )
            {
                // ignore
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // huh?
            }
        }
    }

    @Override
    protected ChecksumPolicy getChecksumPolicy( ProxyRepository proxy, AbstractStorageItem item )
    {
        if ( isChecksum( item.getRepositoryItemUid().getPath() ) )
        {
            // do not validate checksum files
            return null;
        }

        if ( !proxy.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            // we work only with maven proxy reposes, all others are neglected
            return null;
        }

        MavenProxyRepository mpr = proxy.adaptToFacet( MavenProxyRepository.class );

        ChecksumPolicy checksumPolicy = mpr.getChecksumPolicy();

        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum()
            || !( item instanceof DefaultStorageFileItem ) )
        {
            // there is either no need to validate or we can't validate the item content
            return null;
        }

        return checksumPolicy;
    }

    @Override
    protected RemoteHashResponse retrieveRemoteHash( AbstractStorageItem item, ProxyRepository proxy, String baseUrl )
        throws LocalStorageException
    {
        RepositoryItemUid uid = item.getRepositoryItemUid();

        ResourceStoreRequest request = new ResourceStoreRequest( item );

        DefaultStorageFileItem hashItem = null;

        String inspector;
        // we prefer SHA1 ...
        try
        {
            inspector = DigestCalculatingInspector.DIGEST_SHA1_KEY;

            request.pushRequestPath( uid.getPath() + ".sha1" );

            hashItem = doRetriveRemoteChecksumItem( proxy, request );
        }
        catch ( ItemNotFoundException sha1e )
        {
            // ... but MD5 will do too
            inspector = DigestCalculatingInspector.DIGEST_MD5_KEY;
            try
            {
                request.popRequestPath();

                request.pushRequestPath( uid.getPath() + ".md5" );

                hashItem = doRetriveRemoteChecksumItem( proxy, request );
            }
            catch ( ItemNotFoundException md5e )
            {
                getLogger().debug( "Item checksums (SHA1, MD5) remotely unavailable " + uid.toString() );
            }
        }

        String remoteHash = null;

        if ( hashItem != null )
        {
            // store checksum file locally
            hashItem = (DefaultStorageFileItem) proxy.doCacheItem( hashItem );

            // read checksum
            try
            {
                remoteHash = MUtils.readDigestFromFileItem( hashItem );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Cannot read hash string for remotely fetched StorageFileItem: " + uid.toString(), e );
            }
        }

        if ( remoteHash == null )
        {
            return null;
        }

        return new RemoteHashResponse( inspector, remoteHash, hashItem );
    }

    private boolean isChecksum( String path )
    {
        return path.endsWith( ".sha1" ) || path.endsWith( ".md5" );
    }

    private DefaultStorageFileItem doRetriveRemoteChecksumItem( ProxyRepository proxy, ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        try
        {
            return (DefaultStorageFileItem) proxy.getRemoteStorage().retrieveItem( proxy, request, proxy.getRemoteUrl() );
        }
        catch ( RemoteAccessException e )
        {
            throw new ItemNotFoundException( request, proxy, e );
        }
        catch ( RemoteStorageException e )
        {
            throw new ItemNotFoundException( request, proxy, e );
        }
    }
}
