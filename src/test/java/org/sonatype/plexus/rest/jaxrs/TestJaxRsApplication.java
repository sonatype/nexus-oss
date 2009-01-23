package org.sonatype.plexus.rest.jaxrs;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = Application.class )
public class TestJaxRsApplication
    extends Application
{
    @Requirement
    private PlexusObjectFactory objectFactory;

    @Override
    public Set<Class<?>> getClasses()
    {
        return objectFactory.getResourceClasses();
    }
}
