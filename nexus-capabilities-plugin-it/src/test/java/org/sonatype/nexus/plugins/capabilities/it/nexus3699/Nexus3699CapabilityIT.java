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
package org.sonatype.nexus.plugins.capabilities.it.nexus3699;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusRunningITSupport;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.test.CapabilitiesNexusRestClient;

public class Nexus3699CapabilityIT
    extends NexusRunningITSupport
{

    @Inject
    @Named( "${NexusITSupport.capabilitiesPluginCoordinates}" )
    private String capabilitiesPluginCoordinates;

    @Inject
    @Named( "${NexusITSupport.capabilitiesPluginITHelperCoordinates}" )
    private String capabilitiesPluginITHelperCoordinates;

    private static final String TEST_REPOSITORY = "releases";

    private CapabilitiesNexusRestClient capabilitiesNRC;

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins(
            resolveArtifact( capabilitiesPluginCoordinates ),
            resolveArtifact( capabilitiesPluginITHelperCoordinates )
        );
    }

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        capabilitiesNRC = new CapabilitiesNexusRestClient( new NexusRestClient(
            new TestContext()
                .setNexusUrl( nexus().getUrl().toExternalForm() )
                .setSecureTest( true )
        ) );
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
        prop.setValue( "repo_" + TEST_REPOSITORY );
        cap.addProperty( prop );
        prop = new CapabilityPropertyResource();
        prop.setKey( "message" );
        prop.setValue( "Testing CRUD" );
        cap.addProperty( prop );

        CapabilityListItemResource r = capabilitiesNRC.create( cap );
        assertThat( r.getId(), is( notNullValue() ) );
        assertThat( r.getStatus(), is( "<h3>I'm well. Thanx!</h3>" ) );

        // read
        CapabilityResource read = capabilitiesNRC.read( r.getId() );
        assertThat( read.getId(), is( r.getId() ) );
        assertThat( read.getNotes(), is( cap.getNotes() ) );
        assertThat( read.getTypeId(), is( cap.getTypeId() ) );
        assertThat( read.getProperties().size(), is( cap.getProperties().size() ) );

        // update
        read.setNotes( "updateCrudTest" );
        CapabilityListItemResource updated = capabilitiesNRC.update( read );
        assertThat( updated.getNotes(), is( "updateCrudTest" ) );
        read = capabilitiesNRC.read( r.getId() );
        assertThat( read.getNotes(), is( "updateCrudTest" ) );

        // delete
        capabilitiesNRC.delete( r.getId() );
    }

}
