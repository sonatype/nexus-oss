package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

@Component( role = PlexusResource.class, hint = "PlexusUserListPlexusResource" )
public class PlexusUserListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement( role = PlexusUserManager.class )
    private PlexusUserManager userManager;
    
    public PlexusUserListPlexusResource()
    {
        setModifiable( false );
    }
    
    @Override
    public void configureXStream( XStream xstream )
    {
        xstream.omitField( PlexusUserListResourceResponse.class, "modelEncoding" );
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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/plexus_users";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusUserListResourceResponse result = new PlexusUserListResourceResponse();
        
        for ( PlexusUser user : userManager.listUsers() )
        {
            PlexusUserResource res = nexusToRestModel( user, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }
    
    protected PlexusUserResource nexusToRestModel( PlexusUser user, Request request )
    {
        PlexusUserResource resource = new PlexusUserResource();
        
        resource.setUserId( user.getUserId() );
        resource.setName( user.getName() );
        resource.setEmail( user.getEmailAddress() );
        
        for ( PlexusRole role : user.getRoles() )
        {
            PlexusRoleResource roleResource = new PlexusRoleResource();
            roleResource.setRoleId( role.getRoleId() );
            roleResource.setName( role.getName() );
            roleResource.setSource( role.getSource() );
            
            resource.addRole( roleResource );
        }
        
        return resource;
    }
}
