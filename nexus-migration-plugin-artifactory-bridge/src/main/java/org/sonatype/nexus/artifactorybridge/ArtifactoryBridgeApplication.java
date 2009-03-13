package org.sonatype.nexus.artifactorybridge;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusResourceFinder;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = Application.class, hint = "artifactoryBridge" )
public class ArtifactoryBridgeApplication
    extends PlexusRestletApplicationBridge
{

    @Requirement( role = PlexusResource.class, hint = "artifactoryRedirector" )
    private PlexusResource artifactoryRedirector;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        attach( root, false, "", new PlexusResourceFinder( getContext(), artifactoryRedirector ) );
        attach( root, false, "/", new PlexusResourceFinder( getContext(), artifactoryRedirector ) );
    }
}
