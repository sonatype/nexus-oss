package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "UserSearchByIdPlexusResource" )
public class UserSearchByIdPlexusResource extends AbstractUserSearchPlexusResource
{
    public static final String USER_ID_KEY = "userId";
        
    public UserSearchByIdPlexusResource()
    {
        setModifiable( false );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/user_search/*", "authcBasic,perms[nexus:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/user_search/{" + USER_SOURCE_KEY +"}/{" + USER_ID_KEY + "}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        
        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        criteria.setUserId( this.getSearchArg( request ) );
        
        return this.search( criteria, this.getUserSource( request ) );
    }
    
    
    protected String getSearchArg( Request request )
    {
        return request.getAttributes().get( USER_ID_KEY ).toString();
    }
    
}
