/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.matchSha1;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;

public class ClientContentIT
    extends ClientITSupport
{

    private static final String AOP_POM = "aopalliance/aopalliance/1.0/aopalliance-1.0.pom";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ClientContentIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void successfulUploadAndDownloadAndDelete()
        throws IOException
    {
        final Location location = repositoryLocation( "releases", AOP_POM );

        final File toDeploy = testData().resolveFile( "artifacts/" + AOP_POM );
        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "aopalliance-1.0.pom" );

        content().upload( location, toDeploy );
        content().download( location, downloaded );

        assertThat( downloaded, matchSha1( toDeploy ) );

        content().delete( location );
    }

    @Test
    public void wrongUploadLocation()
        throws IOException
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage(
            "Inexistent path: repositories/foo/aopalliance/aopalliance/1.0/aopalliance-1.0.pom"
        );
        content().upload( repositoryLocation( "foo", AOP_POM ), testData().resolveFile( "artifacts/" + AOP_POM ) );
    }

    @Test
    public void wrongDownloadLocation()
        throws IOException
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage(
            "Inexistent path: repositories/foo/aopalliance/aopalliance/1.0/aopalliance-1.0.pom"
        );
        content().download(
            repositoryLocation( "foo", AOP_POM ),
            new File( testIndex().getDirectory( "downloads" ), "aopalliance-1.0.pom" )
        );
    }

    @Test
    public void wrongDeleteLocation()
        throws IOException
    {
        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage(
            "Inexistent path: repositories/foo/aopalliance/aopalliance/1.0/aopalliance-1.0.pom"
        );
        content().delete( repositoryLocation( "foo", AOP_POM ) );
    }

    protected Content content()
    {
        return client().getSubsystem( Content.class );
    }

}
