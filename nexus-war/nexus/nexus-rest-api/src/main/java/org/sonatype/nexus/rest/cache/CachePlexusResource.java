package org.sonatype.nexus.rest.cache;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.descriptors.ClearCacheTaskDescriptor;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "CachePlexusResource" )
public class CachePlexusResource
    extends AbstractRestorePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/data_cache/{" + DOMAIN + "}/{" + TARGET_ID + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/data_index/*/*/content**", "authcBasic,perms[nexus:cache]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            NFCResource resource = new NFCResource();

            // check reposes
            if ( getRepositoryGroupId( request ) != null )
            {
                for ( Repository repository : getNexusInstance( request ).getRepositoryGroup(
                    getRepositoryGroupId( request ) ) )
                {
                    NFCRepositoryResource repoNfc = new NFCRepositoryResource();

                    repoNfc.setRepositoryId( repository.getId() );

                    repoNfc.getNfcPaths().addAll( repository.getNotFoundCache().listKeysInCache() );

                    resource.addNfcContent( repoNfc );
                }
            }
            else if ( getRepositoryId( request ) != null )
            {
                Repository repository = getNexusInstance( request ).getRepository( getRepositoryId( request ) );

                NFCRepositoryResource repoNfc = new NFCRepositoryResource();

                repoNfc.setRepositoryId( repository.getId() );

                repoNfc.getNfcPaths().addAll( repository.getNotFoundCache().listKeysInCache() );

                resource.addNfcContent( repoNfc );
            }

            NFCResourceResponse result = new NFCResourceResponse();

            result.setData( resource );

            return result;
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        ClearCacheTask task = (ClearCacheTask) getNexusInstance( request ).createTaskInstance(
            ClearCacheTaskDescriptor.ID );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        handleDelete( task, request );
    }

}
