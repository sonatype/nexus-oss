package org.sonatype.nexus.testng;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{

    private static final PlexusContainer container;

    public static final PlexusContainer getContainer()
    {
        return container;
    }

    static
    {
        try
        {
            container = setupContainer();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private static synchronized PlexusContainer setupContainer()
        throws Exception
    {
        final File f = new File( "target/plexus-home" );

        if ( !f.isDirectory() )
        {
            f.mkdirs();
        }

        File bundleRoot = new File( TestProperties.getAll().get( "nexus.base.dir" ) );
        System.setProperty( "basedir", bundleRoot.getAbsolutePath() );

        System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            + SeleniumAppBooterCustomizer.class.getName() );

        File classworldsConf = new File( bundleRoot, "conf/classworlds.conf" );

        if ( !classworldsConf.isFile() )
        {
            throw new IllegalStateException( "The bundle classworlds.conf file is not found (\""
                + classworldsConf.getAbsolutePath() + "\")!" );
        }

        System.setProperty( "classworlds.conf", classworldsConf.getAbsolutePath() );

        // this is non trivial here, since we are running Nexus in _same_ JVM as tests
        // and the PlexusAppBooterJSWListener (actually theused WrapperManager in it) enforces then Nexus may be
        // started only once in same JVM!
        // So, we are _overrriding_ the in-bundle plexus app booter with the simplest one
        // since we dont need all the bells-and-whistles in Service and JSW
        // but we are still _reusing_ the whole bundle environment by tricking Classworlds Launcher

        // Launcher trick -- begin
        Launcher launcher = new Launcher();
        launcher.setSystemClassLoader( Thread.currentThread().getContextClassLoader() );
        launcher.configure( new FileInputStream( classworldsConf ) ); // launcher closes stream upon configuration
        // Launcher trick -- end

        // set the preconfigured world
        final PlexusAppBooter plexusAppBooter = new PlexusAppBooter()
        {
            @Override
            protected void customizeContext( Context context )
            {
                super.customizeContext( context );

                context.put( "plexus.app.booter", this );
            }
        };
        plexusAppBooter.setWorld( launcher.getWorld() );

        plexusAppBooter.startContainer();

        PlexusContainer container = plexusAppBooter.getContainer();
        return container;
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
