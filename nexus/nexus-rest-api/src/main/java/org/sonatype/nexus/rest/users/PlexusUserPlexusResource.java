package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.rest.model.PlexusUserResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

@Component( role = PlexusResource.class, hint = "PlexusUserPlexusResource" )
public class PlexusUserPlexusResource
    extends AbstractPlexusUserPlexusResource
{
    public static final String USER_ID_KEY = "userId";
    
    @Requirement( role = PlexusUserManager.class )
    private PlexusUserManager userManager;
    
    public PlexusUserPlexusResource()
    {
        setModifiable( false );
    }
    
    @Override
    public void configureXStream( XStream xstream )
    {
        xstream.omitField( PlexusUserResourceResponse.class, "modelEncoding" );
        xstream.omitField( PlexusUserResource.class, "modelEncoding" );
        xstream.omitField( PlexusRoleResource.class, "modelEncoding" );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_user/*", "authcBasic,perms[nexus:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/plexus_users/{" + USER_ID_KEY + "}";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusUserResourceResponse result = new PlexusUserResourceResponse();

        PlexusUserResource resource = nexusToRestModel( userManager.getUser( getUserId( request ) ), request );
        
        if ( resource == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        
        result.setData( resource );
            
        return result;
    }
    
    protected String getUserId( Request request )
    {
        return request.getAttributes().get( USER_ID_KEY ).toString();
    }
}
