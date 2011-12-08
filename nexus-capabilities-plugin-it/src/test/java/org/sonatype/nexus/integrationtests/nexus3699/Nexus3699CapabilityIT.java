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
package org.sonatype.nexus.integrationtests.nexus3699;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.test.utils.CapabilitiesMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3699CapabilityIT
    extends AbstractNexusIntegrationTest
{

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "capabilities.xml", WORK_CONF_DIR );

        // also need to move the plugin from optional to used
        installOptionalPlugin( "nexus-capabilities-plugin" );
    }

    @Test
    public void crud()
        throws Exception
    {
        // create
        CapabilityResource cap = new CapabilityResource();
        cap.setNotes( "crud-test" );
        cap.setTypeId( "TouchTest" );
        CapabilityPropertyResource prop = new CapabilityPropertyResource();
        prop.setKey( "repoOrGroupId" );
        prop.setValue( "repo_" + REPO_TEST_HARNESS_REPO );
        cap.addProperty( prop );
        prop = new CapabilityPropertyResource();
        prop.setKey( "message" );
        prop.setValue( "Testing CRUD" );
        cap.addProperty( prop );

        CapabilityListItemResource r = CapabilitiesMessageUtil.create( cap );
        assertThat( r.getId(), is(notNullValue()) );
        assertThat( r.getStatus(), is( "<h3>I'm well. Thanx!</h3>" ) );

        // read
        CapabilityResource read = CapabilitiesMessageUtil.read( r.getId() );
        assertThat( read.getId(), is( r.getId() ) );
        assertThat( read.getNotes(), is( cap.getNotes() ) );
        assertThat( read.getTypeId(), is( cap.getTypeId() ) );
        assertThat( read.getProperties().size(), is( cap.getProperties().size() ) );

        // update
        read.setNotes( "updateCrudTest" );
        CapabilityListItemResource updated = CapabilitiesMessageUtil.update( read );
        assertThat( updated.getNotes(), is( "updateCrudTest" ) );
        read = CapabilitiesMessageUtil.read( r.getId() );
        assertThat( read.getNotes(), is( "updateCrudTest" ) );

        // delete
        CapabilitiesMessageUtil.delete( r.getId() );
    }

}
