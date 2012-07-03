/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.testsuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.junit.Test;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;

public class CrudIT
    extends CapabilitiesITSupport
{

    @Test
    public void crud()
    {
        // create
        CapabilityResource cap = new CapabilityResource();
        cap.setNotes( "crud-test" );
        cap.setTypeId( "TouchTest" );
        CapabilityPropertyResource prop = new CapabilityPropertyResource();
        prop.setKey( "repoOrGroupId" );
        prop.setValue( TEST_REPOSITORY );
        cap.addProperty( prop );
        prop = new CapabilityPropertyResource();
        prop.setKey( "message" );
        prop.setValue( "Testing CRUD" );
        cap.addProperty( prop );

        CapabilityListItemResource r = capabilities().add( cap );
        assertThat( r.getId(), is( notNullValue() ) );
        assertThat( r.getStatus(), is( "<h3>I'm well. Thanx!</h3>" ) );

        // read
        CapabilityResource read = capabilities().get( r.getId() );
        assertThat( read.getId(), is( r.getId() ) );
        assertThat( read.getNotes(), is( cap.getNotes() ) );
        assertThat( read.getTypeId(), is( cap.getTypeId() ) );
        assertThat( read.getProperties().size(), is( cap.getProperties().size() ) );

        // list
        final List<CapabilityListItemResource> capabilities = capabilities().list();
        assertThat( capabilities.size(), is( greaterThan( 1 ) ) );

        // update
        read.setNotes( "updateCrudTest" );
        CapabilityListItemResource updated = capabilities().update( read );
        assertThat( updated.getNotes(), is( "updateCrudTest" ) );
        read = capabilities().get( r.getId() );
        assertThat( read.getNotes(), is( "updateCrudTest" ) );

        // delete
        capabilities().delete( r.getId() );
    }

    @Test
    public void enable()
    {
        final CapabilityListItemResource created = createCapability();
        final CapabilityListItemResource enabled = capabilities().enable( created.getId() );

        assertThat( enabled.isEnabled(), is( true ) );
    }

    @Test
    public void disable()
    {
        final CapabilityListItemResource created = createCapability();
        final CapabilityListItemResource enabled = capabilities().disable( created.getId() );

        assertThat( enabled.isEnabled(), is( false ) );
    }

    private CapabilityListItemResource createCapability()
    {
        final CapabilityResource cap = new CapabilityResource();
        cap.setNotes( "crud-test" );
        cap.setTypeId( "TouchTest" );

        {
            final CapabilityPropertyResource prop = new CapabilityPropertyResource();
            prop.setKey( "repoOrGroupId" );
            prop.setValue( TEST_REPOSITORY );
            cap.addProperty( prop );
        }
        {
            final CapabilityPropertyResource prop = new CapabilityPropertyResource();
            prop.setKey( "message" );
            prop.setValue( "Testing CRUD" );
            cap.addProperty( prop );
        }

        return capabilities().add( cap );
    }

}
