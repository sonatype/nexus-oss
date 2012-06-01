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
package org.sonatype.nexus.plugins.capabilities.it.nexus3699;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
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
        prop.setValue( TEST_REPOSITORY );
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
