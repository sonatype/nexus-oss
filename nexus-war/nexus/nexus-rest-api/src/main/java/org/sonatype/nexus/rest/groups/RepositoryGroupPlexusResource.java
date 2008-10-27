package org.sonatype.nexus.rest.groups;

import java.io.IOException;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * Resource handler for Repository resource.
 * 
 * @author tstevens
 * @plexus.component role-hint="RepositoryGroupPlexusResource"
 */
public class RepositoryGroupPlexusResource
    extends AbstractRepositoryGroupPlexusResource
{

    public RepositoryGroupPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryGroupResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*", "authcBasic,perms[nexus:repogroups]" );
    }

    protected String getGroupId( Request request )
    {
        return request.getAttributes().get( GROUP_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryGroupResourceResponse result = new RepositoryGroupResourceResponse();
        try
        {
            CRepositoryGroup group = getNexusInstance( request ).readRepositoryGroup( getGroupId( request ) );

            RepositoryGroupResource resource = new RepositoryGroupResource();

            resource.setId( group.getGroupId() );

            resource.setName( group.getName() );

            resource.setFormat( getNexusInstance( request ).getRepositoryGroupType( group.getGroupId() ) );

            // just to trigger list creation, and not stay null coz of XStream serialization
            resource.getRepositories();

            for ( String repoId : (List<String>) group.getRepositories() )
            {
                RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

                member.setId( repoId );

                member.setName( getNexusInstance( request ).getRepository( repoId ).getName() );

                member.setResourceURI( createChildReference( request, repoId ).toString() );

                resource.addRepository( member );
            }

            result.setData( resource );

        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository declared within a group!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getLogger().warn( "Repository group not found, id=" + getGroupId( request ) );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Group Not Found" );
        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryGroupResourceResponse groupRequest = (RepositoryGroupResourceResponse) payload;
        RepositoryGroupResourceResponse result = null;

        if ( groupRequest != null )
        {
            RepositoryGroupResource resource = groupRequest.getData();

            if ( resource.getRepositories() == null || resource.getRepositories().size() == 0 )
            {
                getLogger().info(
                    "The repository group with ID=" + getGroupId( request ) + " have zero repository members!" );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The group cannot have zero repository members!",
                    getNexusErrorResponse( "repositories", "The group cannot have zero repository members!" ) );
            }

            try
            {
                validateGroup( resource, request );

                CRepositoryGroup group = getNexusInstance( request ).readRepositoryGroup( getGroupId( request ) );

                group.setName( resource.getName() );

                group.getRepositories().clear();

                for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) resource
                    .getRepositories() )
                {
                    group.addRepository( member.getId() );
                }

                getNexusInstance( request ).updateRepositoryGroup( group );
            }
            catch ( NoSuchRepositoryGroupException e )
            {
                getLogger().warn( "Repository group not exists, GroupId=" + getGroupId( request ), e );

                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository group not exists, GroupId="
                    + getGroupId( request ) );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn(
                    "Repository referenced by Repository Group Not Found, GroupId=" + getGroupId( request ),
                    e );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Repository referenced by Repository Group Not Found",
                    getNexusErrorResponse( "repositories", "Repository referenced by Repository Group Not Found" ) );
            }
            catch ( InvalidGroupingException e )
            {
                getLogger().warn( "Invalid grouping, GroupId=" + getGroupId( request ), e );

                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid grouping, GroupId="
                    + getGroupId( request ), getNexusErrorResponse(
                    "repositories",
                    "Repository referenced by Repository Group does not share same content type!" ) );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexusInstance( request ).deleteRepositoryGroup( getGroupId( request ) );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getLogger().warn( "Repository group not found, id=" + getGroupId( request ) );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Group Not Found" );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

}
