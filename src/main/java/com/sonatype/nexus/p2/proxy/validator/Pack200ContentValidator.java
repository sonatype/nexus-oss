/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.proxy.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

import com.sonatype.nexus.p2.proxy.P2ProxyMetadataSource;

@Component( role = ItemContentValidator.class, hint = "Pack200ContentValidator" )
public class Pack200ContentValidator
    extends AbstractLogEnabled
    implements ItemContentValidator
{

    public boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest request, String baseUrl,
                                             AbstractStorageItem item, List<NexusArtifactEvent> events )
        throws StorageException
    {
        final RepositoryItemUid uid = item.getRepositoryItemUid();
        if ( P2ProxyMetadataSource.isP2MetadataItem( uid.getPath() ) )
        {
            return true;
        }

        if ( !uid.getPath().endsWith( ".pack.gz" ) )
        {
            return true;
        }

        if ( !( item instanceof DefaultStorageFileItem ) )
        {
            return true;
        }

        byte[] magicBytes = new byte[4];
        InputStream input = null;

        final RepositoryItemUidLock lock = uid.getLock();
        
        lock.lock( Action.read );
        try
        {
            input = ( (DefaultStorageFileItem) item ).getInputStream();
            input.read( magicBytes );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to read pack200 magic bytes", e );
            return false;
        }
        finally
        {
            IOUtil.close( input );
            lock.unlock();
        }

        return Arrays.equals( magicBytes, new byte[] { 31, -117, 8, 0 } );
    }

}
