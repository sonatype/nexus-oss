package org.sonatype.nexus.plugins.capabilities.internal.config.test;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationLoadEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

public class DefaultCapabilityConfigurationTest
    extends AbstractNexusTestCase
{

    private CapabilityConfiguration configuration;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        configuration = lookup( CapabilityConfiguration.class );
    }

    public void testCrud()
        throws Exception
    {
        assertTrue( configuration.getAll().isEmpty() );

        // create
        CCapability cap = new CCapability();
        cap.setName( "Configuration Test" );
        cap.setTypeId( "AnyTest" );
        configuration.add( cap );

        // make sure it will reload from disk
        configuration.clearCache();

        // read
        assertNull( configuration.get( null ) );
        assertNull( configuration.get( "invalidId" ) );

        CCapability read = configuration.get( cap.getId() );
        assertEquals( read.getId(), cap.getId() );
        assertEquals( read.getName(), cap.getName() );
        assertEquals( read.getTypeId(), cap.getTypeId() );
        assertEquals( read.getProperties().size(), cap.getProperties().size() );


        // update
        cap.setName( "NewCapName" );
        configuration.update( cap );
        configuration.clearCache();
        read = configuration.get( cap.getId() );
        assertEquals( read.getName(), cap.getName() );

        // load eventing
        final List<CapabilityConfigurationLoadEvent> events = new ArrayList<CapabilityConfigurationLoadEvent>();
        ApplicationEventMulticaster applicationEventMulticaster = lookup( ApplicationEventMulticaster.class );
        applicationEventMulticaster.addEventListener( new EventListener()
        {
            public void onEvent( Event<?> evt )
            {
                if ( evt instanceof CapabilityConfigurationLoadEvent )
                {
                    events.add( (CapabilityConfigurationLoadEvent) evt );
                }
            }
        } );
        configuration.load();
        assertEquals( 1, events.size() );

        // delete
        configuration.remove( cap.getId() );
        assertTrue( configuration.getAll().isEmpty() );

        configuration.clearCache();
        assertTrue( configuration.getAll().isEmpty() );
    }

}
