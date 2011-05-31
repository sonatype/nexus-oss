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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.osgi.impl.bundle.obr.resource.BundleInfo;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.obr.ObrPluginConfiguration;
import org.sonatype.nexus.obr.util.ObrUtils;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Bindex based {@link ObrMetadataSource} component.
 */
@Component( role = ObrMetadataSource.class, hint = "obr-bindex", description = "bindex" )
public class DefaultObrMetadataSource
    extends AbstractLogEnabled
    implements ObrMetadataSource
{
    @Requirement
    private ObrPluginConfiguration obrConfiguration;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement
    private MimeUtil mimeUtil;

    public ObrResourceReader getReader( final ObrSite site )
        throws StorageException
    {
        try
        {
            return new DefaultObrResourceReader( site, obrConfiguration.isBundleCacheActive() );
        }
        catch ( final IOException e )
        {
            throw new StorageException( e );
        }
    }

    public Resource buildResource( final StorageFileItem item )
    {
        if ( !ObrUtils.acceptItem( item ) )
        {
            return null; // ignore non-OBR resource items
        }

        InputStream is = null;

        try
        {
            is = item.getInputStream();
            if ( is != null )
            {
                final RepositoryItemUid uid = item.getRepositoryItemUid();
                final BundleInfo info = new BundleInfo( null, is, "file:" + uid.getPath(), item.getLength() );
                if ( info.isOSGiBundle() )
                {
                    return info.build();
                }
            }
        }
        catch ( final Exception e )
        {
            getLogger().warn( "Unable to generate OBR metadata for item " + item.getRepositoryItemUid(), e );
        }
        finally
        {
            IOUtil.close( is );
        }

        return null;
    }

    public ObrResourceWriter getWriter( final RepositoryItemUid uid )
        throws StorageException
    {
        try
        {
            return new DefaultObrResourceWriter( uid, nexusConfiguration.getTemporaryDirectory(), mimeUtil );
        }
        catch ( final IOException e )
        {
            throw new StorageException( e );
        }
    }
}
