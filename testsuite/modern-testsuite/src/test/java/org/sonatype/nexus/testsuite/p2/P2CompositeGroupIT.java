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
package org.sonatype.nexus.testsuite.p2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;

import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.repository.p2.client.P2CompositeGroupRepository;
import org.sonatype.nexus.repository.p2.client.P2ProxyRepository;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.filesystem.Get;

public class P2CompositeGroupIT
    extends P2ITSupport
{

    private Server remoteServer;

    public P2CompositeGroupIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Before
    public void startRemoteServer()
        throws Exception
    {
        final Get getBehavior = Behaviours.get( testData().resolveFile( "proxy-repo" ) );
        remoteServer = Server
            .withPort( 0 )
            .serve( "/*" ).withBehaviours(
                new Behaviour()
                {
                    @Override
                    public boolean execute( final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final Map<Object, Object> ctx )
                        throws Exception
                    {
                        response.setDateHeader( "Last-Modified", System.currentTimeMillis() );
                        return getBehavior.execute( request, response, ctx );
                    }
                }
            )
            .start();
    }

    @After
    public void stopRemoteServer()
        throws Exception
    {
        if ( remoteServer != null )
        {
            remoteServer.stop();
        }
    }

    @Test
    public void groupHasCompositeXmls()
        throws Exception
    {
        final P2ProxyRepository proxy1 =
            repositories().create( P2ProxyRepository.class, repositoryIdForTest( "proxy-1" ) )
                .asProxyOf( remoteServer.getUrl().toExternalForm() + "/repo-1/" )
                .doNotAutoBlock()
                .save();

        final P2ProxyRepository proxy2 =
            repositories().create( P2ProxyRepository.class, repositoryIdForTest( "proxy-2" ) )
                .asProxyOf( remoteServer.getUrl().toExternalForm() + "/repo-2/" )
                .doNotAutoBlock()
                .save();

        final P2CompositeGroupRepository group =
            repositories().create( P2CompositeGroupRepository.class, repositoryIdForTest( "group" ) )
                .addMember( proxy1.id() )
                .addMember( proxy2.id() )
                .save();

        logger.info( "Downloading artifacts.xml from {}", proxy1.id() );
        content().download(
            repositoryLocation( proxy1.id(), "artifacts.xml" ),
            new File( testIndex().getDirectory( "downloads/" + proxy1.id() ), "artifacts.xml" )
        );
        logger.info( "Downloading content.xml from {}", proxy1.id() );
        content().download(
            repositoryLocation( proxy1.id(), "content.xml" ),
            new File( testIndex().getDirectory( "downloads/" + proxy1.id() ), "content.xml" )
        );

        logger.info( "Downloading artifacts.xml from {}", proxy2.id() );
        content().download(
            repositoryLocation( proxy2.id(), "artifacts.xml" ),
            new File( testIndex().getDirectory( "downloads/" + proxy2.id() ), "artifacts.xml" )
        );
        logger.info( "Downloading content.xml from {}", proxy2.id() );
        content().download(
            repositoryLocation( proxy2.id(), "content.xml" ),
            new File( testIndex().getDirectory( "downloads/" + proxy2.id() ), "content.xml" )
        );

        // check that group contains compositeArtifacts.xml, compositeContent.xml and p2.index
        logger.info( "Downloading p2.index from {}", group.id() );
        content().download(
            repositoryLocation( group.id(), "p2.index" ),
            new File( testIndex().getDirectory( "downloads/" + group.id() ), "p2.index" )
        );
        final File compositeArtifacts = new File(
            testIndex().getDirectory( "downloads/" + group.id() ), "compositeArtifacts.xml"
        );
        logger.info( "Downloading compositeArtifacts.xml from {}", group.id() );
        content().download(
            repositoryLocation( group.id(), "compositeArtifacts.xml" ),
            compositeArtifacts
        );
        final File compositeContent = new File(
            testIndex().getDirectory( "downloads/" + group.id() ), "compositeContent.xml"
        );
        logger.info( "Downloading compositeContent.xml from {}", group.id() );
        content().download(
            repositoryLocation( group.id(), "compositeContent.xml" ),
            compositeContent
        );

        // verify that composite artifacts contains references to p2 proxy repositories
        assertThat( compositeArtifacts, contains( proxy1.id(), proxy2.id() ) );
        assertThat( compositeContent, contains( proxy1.id(), proxy2.id() ) );

        // verify that we can download files via group
        logger.info( "Downloading com.sonatype.nexus.p2.its.bundle_1.0.0.jar from {}", group.id() );
        content().download(
            repositoryLocation( group.id(), proxy1.id() + "/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" ),
            compositeContent
        );
        logger.info( "Downloading com.sonatype.nexus.p2.its.feature2_1.0.0.jar from {}", group.id() );
        content().download(
            repositoryLocation( group.id(), proxy2.id() + "/features/com.sonatype.nexus.p2.its.feature2_1.0.0.jar" ),
            compositeContent
        );
    }

}
