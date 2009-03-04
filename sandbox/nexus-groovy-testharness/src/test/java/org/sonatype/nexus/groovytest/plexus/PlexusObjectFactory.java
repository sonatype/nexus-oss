package org.sonatype.nexus.groovytest.plexus;

import java.lang.reflect.Constructor;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{

    private PlexusContainer container;

    public PlexusObjectFactory()
        throws PlexusContainerException
    {
        container = new DefaultPlexusContainer();
    }

    private static final long serialVersionUID = -45456541236971L;

    public Object newInstance( Constructor constructor, Object... params )
    {
        String role = constructor.getDeclaringClass().getName();
        String hint = null;
        if ( params != null && params.length == 1 && params[0] instanceof String )
        {
            hint = (String) params[0];
        }

        try
        {
            if ( hint != null )
            {
                return container.lookup( role, hint );
            }
            else
            {
                return container.lookup( role );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }

    }

}
