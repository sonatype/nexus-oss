package org.sonatype.nexus.rest.users;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.nexus.rest.component.AbstractComponentListPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "UserLocatorComponentListPlexusResource" )
public class UserLocatorComponentListPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/userLocators";
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentsuserlocatortypes]" );
    }

    @Override
    protected String getRole( Request request )
    {
        return PlexusUserLocator.class.getName();
    }

}
