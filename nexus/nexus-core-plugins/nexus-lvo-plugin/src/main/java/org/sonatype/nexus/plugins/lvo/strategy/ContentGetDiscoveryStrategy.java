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
package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This is a "local" strategy, uses Nexus content for information fetch.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "content-get" )
public class ContentGetDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        StorageFileItem response = handleRequest( request );

        if ( response != null )
        {
            InputStream is = response.getInputStream();

            try
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                IOUtil.copy( is, bos );

                dr.setVersion( bos.toString() );

                dr.setSuccessful( true );
            }
            finally
            {
                IOUtil.close( is );
            }
        }

        return dr;
    }

    protected StorageFileItem handleRequest( DiscoveryRequest request )
    {
        try
        {
            // NoSuchRepository if the repoId is not known
            Repository repository = repositoryRegistry.getRepository( request.getLvoKey().getRepositoryId() );

            // ItemNotFound if the path does not exists
            StorageItem item = repository.retrieveItem( false, new ResourceStoreRequest( request
                .getLvoKey().getLocalPath() ) );

            // return only if item is a file, nuke it otherwise
            if ( item instanceof StorageFileItem )
            {
                return (StorageFileItem) item;
            }
            else
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            // we are very rude about exceptions here ;)
            getLogger().warn( "Could not retrieve content!", e );

            return null;
        }
    }
}
