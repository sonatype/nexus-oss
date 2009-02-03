package org.sonatype.nexus.rest.privileges;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.privileges.PrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.nexus.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.nexus.rest.model.PrivilegeTypeResource;
import org.sonatype.nexus.rest.model.PrivilegeTypeResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "PrivilegeTypePlexusResource" )
public class PrivilegeTypePlexusResource
    extends AbstractPrivilegePlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:privilegetypes]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/privilege_types";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeTypeResourceResponse result = new PrivilegeTypeResourceResponse();

        List<PrivilegeDescriptor> privDescriptors = getNexusSecurity().listPrivilegeDescriptors();

        for ( PrivilegeDescriptor privDescriptor : privDescriptors )
        {
            PrivilegeTypeResource type = new PrivilegeTypeResource();
            type.setId( privDescriptor.getType() );
            type.setName( privDescriptor.getName() );
            
            for ( PrivilegePropertyDescriptor propDescriptor : privDescriptor.getPropertyDescriptors() )
            {
                PrivilegeTypePropertyResource typeProp = new PrivilegeTypePropertyResource();
                typeProp.setId( propDescriptor.getId() );
                typeProp.setName( propDescriptor.getName() );
                typeProp.setHelpText( propDescriptor.getHelpText() );
                
                type.addProperty( typeProp );
            }
            
            result.addData( type );
        }

        return result;
    }
}
