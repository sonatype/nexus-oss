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
package org.sonatype.nexus.plugins.p2.repository.proxy.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.p2.repository.proxy.P2ProxyMetadataSource;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

@Component( role = ItemContentValidator.class, hint = "Pack200ContentValidator" )
public class Pack200ContentValidator
    extends AbstractLogEnabled
    implements ItemContentValidator
{
    private final byte[] PACK200_MAGIC = new byte[] { 31, -117, 8, 0 };

    private final byte[] JAR_MAGIC = new byte[] { 80, 75, 3, 4 };

    @Override
    public boolean isRemoteItemContentValid( final ProxyRepository proxy, final ResourceStoreRequest request,
                                             final String baseUrl, final AbstractStorageItem item,
                                             final List<RepositoryItemValidationEvent> events )
        throws LocalStorageException
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

        final byte[] magicBytes = new byte[4];
        InputStream input = null;

        try
        {
            input = ( (DefaultStorageFileItem) item ).getInputStream();
            input.read( magicBytes );
        }
        catch ( final IOException e )
        {
            getLogger().error( "Unable to read pack200 magic bytes", e );
            return false;
        }
        finally
        {
            IOUtil.close( input );
        }

        return Arrays.equals( magicBytes, PACK200_MAGIC ) // real pack.gz
            || Arrays.equals( magicBytes, JAR_MAGIC ) // plain jar works too
        ;
    }

}
