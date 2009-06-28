package org.sonatype.nexus.plugins;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.context.Context;

public abstract class AbstractNexusPluginManagerTest
    extends PlexusTestCase
{
    protected ClassWorld classWorld;
    
    protected DefaultNexusPluginManager nexusPluginManager;

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "nexus-work", getTestFile( "src/test" ).getAbsolutePath() );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration config )
    {
        classWorld = new ClassWorld();
        
        try
        {
            ClassRealm realm = classWorld.newRealm( "plexus.core", Thread.currentThread().getContextClassLoader() );

            Thread.currentThread().setContextClassLoader( realm );
        }
        catch ( DuplicateRealmException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            fail();
        }

        config.setClassWorld( classWorld );
        
        config.setComponentRepository( new NexusPluginsComponentRepository() );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }
}
