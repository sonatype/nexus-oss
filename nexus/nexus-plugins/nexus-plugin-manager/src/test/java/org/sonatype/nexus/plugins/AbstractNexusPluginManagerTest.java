package org.sonatype.nexus.plugins;

import java.net.MalformedURLException;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.context.Context;

public abstract class AbstractNexusPluginManagerTest
    extends PlexusTestCase
{
    protected ClassWorld classWorld;

    protected ClassRealm plexusCoreRealm;

    protected DefaultNexusPluginManager nexusPluginManager;

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "nexus-work", getTestFile( "src/test" ).getAbsolutePath() );

        context.put( "nexus-app", getTestFile( "target/nexus-app" ).getAbsolutePath() );

        context.put( ComponentRepository.class.getName(), new NexusPluginsComponentRepository() );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration config )
    {
        classWorld = new ClassWorld();

        try
        {
            plexusCoreRealm = classWorld.newRealm( "plexus.core", Thread.currentThread().getContextClassLoader() );

            plexusCoreRealm.addURL( getTestFile( "target/classes/" ).toURI().toURL() );

            plexusCoreRealm.addURL( getTestFile( "target/test-classes/" ).toURI().toURL() );
        }
        catch ( DuplicateRealmException e )
        {
            e.printStackTrace();

            fail();
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();

            fail();
        }

        config.setClassWorld( classWorld );

        config.setRealm( plexusCoreRealm );

        config.setComponentRepository( (ComponentRepository) config.getContext().get(
                                                                                      ComponentRepository.class
                                                                                          .getName() ) );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }
}
