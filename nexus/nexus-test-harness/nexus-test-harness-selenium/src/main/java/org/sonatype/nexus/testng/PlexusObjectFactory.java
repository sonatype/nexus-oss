package org.sonatype.nexus.testng;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.mock.InhibitingComponentDiscovererListener;
import org.sonatype.nexus.mock.util.ContainerUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{

    private static final PlexusContainer container = setupContainer();

    private static synchronized PlexusContainer setupContainer()
    {
        File f = new File( "target/plexus-home" );

        if ( !f.isDirectory() )
        {
            f.mkdirs();
        }

        Map<Object, Object> context = ContainerUtil.createContainerContext();
        context.put( "plexus.home", f.getAbsolutePath() );
        context.putAll( TestProperties.getAll() );

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration cc = new DefaultContainerConfiguration();
        cc.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );
        cc.setContext( context );
        cc.addComponentDiscoveryListener( new InhibitingComponentDiscovererListener() );

        try
        {
            return new DefaultPlexusContainer( cc );
        }
        catch ( PlexusContainerException e )
        {
            throw new RuntimeException( "Failed to create plexus container: " + e.getMessage(), e );
        }
    }

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
