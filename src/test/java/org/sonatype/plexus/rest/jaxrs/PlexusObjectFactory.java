package org.sonatype.plexus.rest.jaxrs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.sonatype.plexus.rest.jsr311.JsrComponent;

@Component( role = PlexusObjectFactory.class )
public class PlexusObjectFactory
    implements ObjectFactory
{
    @Requirement( role = JsrComponent.class )
    private Map<String, Object> hinstsToresources;

    /** A lookup map filled in by getResourceClasses */
    private Map<Class<?>, Object> classesToComponents;

    public Set<Class<?>> getResourceClasses()
    {
        classesToComponents = new HashMap<Class<?>, Object>( hinstsToresources.size() );

        for ( Object res : hinstsToresources.values() )
        {
            classesToComponents.put( res.getClass(), res );
        }

        return classesToComponents.keySet();
    }

    public <T> T getInstance( Class<T> jaxRsClass )
        throws InstantiateException
    {
        if ( classesToComponents.containsKey( jaxRsClass ) )
        {
            return (T) classesToComponents.get( jaxRsClass );
        }

        throw new InstantiateException( "JsrComponent of class '" + jaxRsClass.getName() + "' not found!" );
    }

}
