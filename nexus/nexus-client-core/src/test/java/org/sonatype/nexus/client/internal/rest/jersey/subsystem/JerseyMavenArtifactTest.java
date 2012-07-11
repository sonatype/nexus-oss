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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem;

import java.net.MalformedURLException;

import org.junit.Test;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.core.subsystem.artifact.ArtifactMaven;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveRequest;
import org.sonatype.nexus.client.core.subsystem.artifact.ResolveResponse;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.client.rest.jersey.subsystem.JerseyArtifactMavenSubsystemFactory;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import junit.framework.Assert;

public class JerseyMavenArtifactTest
    extends TestSupport
{

    @Test
    public void artifactMavenResolveSuccess()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance( new JerseyArtifactMavenSubsystemFactory() );

        final ArtifactMaven artifactMaven = client.getSubsystem( ArtifactMaven.class );
        Assert.assertNotNull( artifactMaven );

        final ResolveRequest resolveRequest =
            new ResolveRequest( "central-proxy", "org.slf4j", "slf4j-api", ResolveRequest.VERSION_RELEASE );
        final ResolveResponse resolveResponse = artifactMaven.resolve( resolveRequest );
        Assert.assertNotNull( resolveResponse );
        Assert.assertEquals( "org.slf4j", resolveResponse.getGroupId() );
        Assert.assertEquals( "slf4j-api", resolveResponse.getArtifactId() );
        // Assert.assertEquals( "", resolveResponse.getVersion() ); RSO might change
        Assert.assertEquals( "jar", resolveResponse.getExtension() );
        Assert.assertEquals( false, resolveResponse.isSnapshot() );

        System.out.println( resolveResponse.getVersion() );
        System.out.println( resolveResponse.getSha1() );
        System.out.println( resolveResponse.getRepositoryPath() );
    }

    @Test
    public void artifactMavenResolveFailure()
        throws MalformedURLException
    {
        final NexusClient client = createClientForLiveInstance( new JerseyArtifactMavenSubsystemFactory() );

        final ArtifactMaven artifactMaven = client.getSubsystem( ArtifactMaven.class );
        Assert.assertNotNull( artifactMaven );

        // there is no commercial nexus plugin in Central
        final ResolveRequest resolveRequest =
            new ResolveRequest( "central-proxy", "com.sonatype.nexus.plugin", "nexus-staging-plugin",
                                ResolveRequest.VERSION_RELEASE );
        final ResolveResponse resolveResponse = artifactMaven.resolve( resolveRequest );
        Assert.assertNull( resolveResponse );
    }

    /**
     * This method is a CHEAT! It would need to prepare and locally run a Nexus instance, but for now, RSO is used...
     * Naturally, this makes the tests unstable too...
     *
     * @return
     * @throws MalformedURLException
     */
    protected NexusClient createClientForLiveInstance(
        final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories )
        throws MalformedURLException
    {
        final NexusClientFactory factory = new JerseyNexusClientFactory( subsystemFactories );
        final NexusClient client = factory.createFor( BaseUrl.baseUrlFrom( "https://repository.sonatype.org/" ) );
        return client;
    }
}
