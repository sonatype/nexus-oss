package org.sonatype.security.sample.web;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.PlexusPathMatchingFilterChainResolver;

@Component( role = Application.class, hint = "secureApplication" )
public class PlexusSecureApplication
    extends PlexusRestletApplicationBridge
{

    @Requirement
    private PlexusPathMatchingFilterChainResolver plexusPathMatchingFilterChainResolver;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        super.doCreateRoot( root, isStarted );

        this.plexusPathMatchingFilterChainResolver.addProtectedResource( "/**", "authcBasic,perms[sample:permToCatchAllUnprotecteds]" );
    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        this.plexusPathMatchingFilterChainResolver.addProtectedResource( descriptor
                                                                         .getPathPattern(), descriptor.getFilterExpression() );
       
    }
}
