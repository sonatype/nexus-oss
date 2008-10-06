package org.sonatype.plexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Application;
import org.restlet.Router;

@Component( role = Application.class, hint = "test" )
public class TestApplication
    extends PlexusRestletApplicationBridge
{
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        root.attach( "/manual", SimpleRestletResource.class );
    }
}
