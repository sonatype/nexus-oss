package org.sonatype.nexus.rest.authentication;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.authentication.AbstractLoginPlexusResource;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author bdemers
 */
@Component( role = PlexusResource.class, hint = "LoginPlexusResource" )
public class NexusLogingPlexusResource
    extends AbstractLoginPlexusResource
{
    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:authentication]" );
    }
}
