/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package com.sonatype.nexus.unpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "UnpackPlexusResource" )
public class UnpackResource
    extends AbstractResourceStoreContentPlexusResource
{

    public UnpackResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/content-compressed/**", "authcBasic,trperms" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY + "}/content-compressed";
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

    @Override
    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        try
        {
            Repository repository = getResourceStore( request );

            String basePath = getResourceStorePath( request );

            if ( basePath.toLowerCase().endsWith( ".md5" ) || basePath.toLowerCase().endsWith( ".sha1" ) )
            {
                // maven deploys checksums even if not asked to
                return null;
            }

            try
            {
                StorageItem item = repository.retrieveItem( getResourceStoreRequest( request, basePath ) );

                deleteItem( repository, item );
            }
            catch ( ItemNotFoundException e )
            {
                // that's good
            }

            for ( FileItem fileItem : files )
            {
                File tempFile = File.createTempFile( "unzip", ".zip" );
                try
                {
                    copyToFile( fileItem, tempFile );

                    ZipFile zip = new ZipFile( tempFile );
                    try
                    {
                        Enumeration<? extends ZipEntry> entries = zip.entries();

                        while ( entries.hasMoreElements() )
                        {
                            ZipEntry entry = entries.nextElement();

                            if ( entry.getName().endsWith( "/" ) )
                            {
                                // must be a folder
                                continue;
                            }

                            ResourceStoreRequest storeRequest =
                                getResourceStoreRequest( request, basePath + "/" + entry.getName() );

                            InputStream is = zip.getInputStream( entry );
                            try
                            {
                                repository.storeItem( storeRequest, is, null );
                            }
                            finally
                            {
                                IOUtil.close( is );
                            }
                        }
                    }
                    finally
                    {
                        close( zip );
                    }
                }
                finally
                {
                    tempFile.delete();
                }
            }
        }
        catch ( Exception t )
        {
            handleException( request, response, t );
        }

        return null;
    }

    private void deleteItem( Repository repository, StorageItem item )
        throws AccessDeniedException, StorageException, NoSuchResourceStoreException, IllegalOperationException,
        ItemNotFoundException, UnsupportedStorageOperationException
    {
        if ( item instanceof StorageCollectionItem )
        {
            for ( StorageItem child : ( (StorageCollectionItem) item ).list() )
            {
                deleteItem( repository, child );
            }
        }
        else
        {
            repository.deleteItem( item.getResourceStoreRequest() );
        }
    }

    private void close( ZipFile zip )
    {
        try
        {
            zip.close();
        }
        catch ( IOException e )
        {
            getLogger().debug( "Could not close ZipFile", e );
        }
    }

    private void copyToFile( FileItem source, File target )
        throws IOException
    {
        InputStream is = source.getInputStream();
        try
        {
            FileUtils.copyStreamToFile( new RawInputStreamFacade( is ), target );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    @Override
    protected Repository getResourceStore( Request request )
        throws NoSuchResourceStoreException, ResourceException
    {
        String repoId = request.getAttributes().get( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString();

        Repository repository = getUnprotectedRepositoryRegistry().getRepository( repoId );

        return repository;
    }
}
