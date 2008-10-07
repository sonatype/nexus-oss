package org.sonatype.nexus.rest.restore;

import java.util.concurrent.RejectedExecutionException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

public abstract class AbstractRestorePlexusResource extends AbstractNexusPlexusResource
{

    public static final String DOMAIN = "domain";

    public static final String DOMAIN_REPOSITORIES = "repositories";

    public static final String DOMAIN_REPO_GROUPS = "repo_groups";

    public static final String TARGET_ID = "target";

    
//    protected String getRepositoryId()
//    {
////        return repositoryId;
//    }
//
//    protected String getRepositoryGroupId()
//    {
////        return repositoryGroupId;
//    }
//
//    protected String getResourceStorePath()
//    {
////        return resourceStorePath;
//    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
//            // check reposes
//            if ( repositoryGroupId != null )
//            {
//                getNexusInstance( request ).readRepositoryGroup( repositoryGroupId );
//            }
//            else if ( repositoryId != null )
//            {
//                try
//                {
//                    getNexusInstance( request ).readRepository( repositoryId );
//                }
//                catch ( NoSuchRepositoryException e )
//                {
//                    getNexusInstance( request ).readRepositoryShadow( repositoryId );
//                }
//            }
//
//            getNexusInstance( request ).submit( "Internal", task );
            
        }
        catch ( RejectedExecutionException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
        }
//        catch ( NoSuchRepositoryException e )
//        {
//            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
//        }
//        catch ( NoSuchRepositoryGroupException e )
//        {
//            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
//        }
    }
    
    
}
