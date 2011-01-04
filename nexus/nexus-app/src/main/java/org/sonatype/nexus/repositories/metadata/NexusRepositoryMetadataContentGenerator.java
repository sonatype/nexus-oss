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
package org.sonatype.nexus.repositories.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = NexusRepositoryMetadataContentGenerator.ID )
public class NexusRepositoryMetadataContentGenerator
    implements ContentGenerator
{
    public static final String ID = "NexusRepositoryMetadataContentGenerator";

    @Override
    public String getGeneratorId()
    {
        return ID;
    }

    @Override
    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = null;

        try
        {
            is = item.getInputStream();

            IOUtil.copy( is, bos );

            String body = new String( bos.toByteArray(), "UTF-8" );

            StringContentLocator result = null;

            if ( item.getItemContext().getRequestAppRootUrl() != null )
            {
                String appRootUrl = item.getItemContext().getRequestAppRootUrl();

                // trim last slash NEXUS-1736
                if ( appRootUrl.endsWith( "/" ) )
                {
                    appRootUrl = appRootUrl.substring( 0, appRootUrl.length() - 1 );
                }

                result = new StringContentLocator( body.replace( "@rootUrl@", appRootUrl ) );
            }
            else
            {
                result = new StringContentLocator( body.replace( "@rootUrl@", "" ) );
            }

            item.setLength( result.getByteArray().length );

            return result;
        }
        catch ( IOException e )
        {
            throw new LocalStorageException( e );
        }
        finally
        {
            IOUtil.close( is );
        }
    }
}
