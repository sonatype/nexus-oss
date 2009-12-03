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
        // this is the ONLY resource using authcNxBasic, as the UI can't receive 401 errors from teh server
        // as the browser login pops up, which is no good in this case
        return new PathProtectionDescriptor( getResourceUri(), "authcNxBasic,perms[nexus:authentication]" );
    }
}
