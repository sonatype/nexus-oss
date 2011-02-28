package org.sonatype.security.web.testapp;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.ProtectedPathManager;

@Singleton
@Typed( value = Application.class )
@Named( value = "secureApplication" )
public class PlexusSecureApplication
    extends PlexusRestletApplicationBridge
{

    @Inject
    private ProtectedPathManager protectedPathManager;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        super.doCreateRoot( root, isStarted );

        this.protectedPathManager.addProtectedResource( "/**", "authcBasic,perms[sample:permToCatchAllUnprotecteds]" );
    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        this.protectedPathManager.addProtectedResource( descriptor
                                                                         .getPathPattern(), descriptor.getFilterExpression() );
       
    }
}
