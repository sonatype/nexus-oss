/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.config;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import com.google.common.eventbus.Subscribe;

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

    @Test
    public void testCrud()
        throws Exception
    {
        assertTrue( configuration.getAll().isEmpty() );

        // create
        CCapability cap = new CCapability();
        cap.setNotes( "Configuration Test" );
        cap.setTypeId( DefaultCapabilityConfigurationTestDescriptor.ID );
        configuration.add( cap );

        // make sure it will reload from disk
        configuration.clearCache();

        // read
        assertNull( configuration.get( null ) );
        assertNull( configuration.get( "invalidId" ) );

        CCapability read = configuration.get( cap.getId() );
        assertEquals( read.getId(), cap.getId() );
        assertEquals( read.getDescription(), cap.getDescription() );
        assertEquals( read.getNotes(), cap.getNotes() );
        assertEquals( read.getTypeId(), cap.getTypeId() );
        assertEquals( read.getProperties().size(), cap.getProperties().size() );

        // update
        cap.setNotes( "NewCapDescription" );
        configuration.update( cap );
        configuration.clearCache();
        read = configuration.get( cap.getId() );
        assertEquals( read.getDescription(), cap.getDescription() );
        assertEquals( read.getNotes(), cap.getNotes() );

        // load eventing
        final List<CapabilityConfigurationEvent.Loaded> events = new ArrayList<CapabilityConfigurationEvent.Loaded>();
        NexusEventBus eventBus = lookup( NexusEventBus.class );
        eventBus.register( new Object()
        {
            @Subscribe
            public void handle( CapabilityConfigurationEvent.Loaded evt )
            {
                events.add( evt );
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
