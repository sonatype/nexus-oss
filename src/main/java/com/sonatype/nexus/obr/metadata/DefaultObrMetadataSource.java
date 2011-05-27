/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.metadata;

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
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

import com.sonatype.nexus.obr.ObrPluginConfiguration;
import com.sonatype.nexus.obr.util.ObrUtils;

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

    public ObrResourceReader getReader( ObrSite site )
        throws StorageException
    {
        try
        {
            return new DefaultObrResourceReader( site, obrConfiguration.isBundleCacheActive() );
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }
    }

    public Resource buildResource( StorageFileItem item )
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
                RepositoryItemUid uid = item.getRepositoryItemUid();
                BundleInfo info = new BundleInfo( null, is, "file:" + uid.getPath(), item.getLength() );
                if ( info.isOSGiBundle() )
                {
                    return info.build();
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Unable to generate OBR metadata for item " + item.getRepositoryItemUid(), e );
        }
        finally
        {
            IOUtil.close( is );
        }

        return null;
    }

    public ObrResourceWriter getWriter( RepositoryItemUid uid )
        throws StorageException
    {
        try
        {
            return new DefaultObrResourceWriter( uid, nexusConfiguration.getTemporaryDirectory(), mimeUtil );
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }
    }
}
