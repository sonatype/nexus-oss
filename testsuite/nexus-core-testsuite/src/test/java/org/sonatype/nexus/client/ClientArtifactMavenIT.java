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
package org.sonatype.nexus.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.artifact.ArtifactMaven;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveRequest;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveResponse;
import org.sonatype.nexus.client.core.subsystem.content.Content;

public class ClientArtifactMavenIT
    extends ClientITSupport
{

    private static final String AOP_POM = "aopalliance/aopalliance/1.0/aopalliance-1.0.pom";

    private static final String AOP_JAR = "aopalliance/aopalliance/1.0/aopalliance-1.0.jar";

    private static final String AOP_META = "aopalliance/aopalliance/maven-metadata.xml";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ClientArtifactMavenIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void artifactMavenResolveSuccess()
        throws IOException
    {
        upload( AOP_POM );
        upload( AOP_JAR );
        upload( AOP_META );

        final ResolveResponse response = artifacts().resolve(
            new ResolveRequest(
                "releases", "aopalliance", "aopalliance", ResolveRequest.VERSION_RELEASE
            )
        );
        assertThat( response, is( notNullValue() ) );
        assertThat( response.getGroupId(), is( "aopalliance" ) );
        assertThat( response.getArtifactId(), is( "aopalliance" ) );
        assertThat( response.getExtension(), is( "jar" ) );
        assertThat( response.isSnapshot(), is( false ) );
    }

    @Test
    public void artifactMavenResolveFailure()
    {
        thrown.expect( NexusClientNotFoundException.class );
        artifacts().resolve(
            new ResolveRequest(
                "releases", "com.sonatype.nexus.plugin", "nexus-staging-plugin", ResolveRequest.VERSION_RELEASE
            )
        );
    }

    private void upload( final String path )
        throws IOException
    {
        content().upload( repositoryLocation( "releases", path ), testData().resolveFile( "artifacts/" + path ) );
    }

    private ArtifactMaven artifacts()
    {
        return client().getSubsystem( ArtifactMaven.class );
    }

    protected Content content()
    {
        return client().getSubsystem( Content.class );
    }

}
