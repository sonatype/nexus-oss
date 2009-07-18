package org.sonatype.nexus.testng;

import java.lang.reflect.Constructor;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.mock.TestContext;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{

    private static final long serialVersionUID = -45456541236971L;

    @SuppressWarnings( "unchecked" )
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
                return TestContext.getContainer().lookup( role, hint );
            }
            else
            {
                return TestContext.getContainer().lookup( role );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }

    }

}
