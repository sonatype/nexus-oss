package org.sonatype.plexus.rest.jaxrs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.rest.jsr311.JsrComponent;

@Component( role = Application.class )
public class TestJaxRsApplication
    extends Application
{
    @Requirement( role = JsrComponent.class )
    private List<Object> resources;

    @Override
    public Set<Class<?>> getClasses()
    {
        return Collections.emptySet();
    }

    public Set<Object> getSingletons()
    {
        HashSet<Object> result = new HashSet<Object>();

        result.addAll( resources );

        return result;
    }
}
