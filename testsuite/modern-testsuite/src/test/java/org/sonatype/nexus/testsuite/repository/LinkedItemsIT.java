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
package org.sonatype.nexus.testsuite.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.matchSha1;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.testsuite.NexusCoreITSupport;
import org.sonatype.nexus.testsuite.client.LinkedItems;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

/**
 * ITs related to storage linked items.
 *
 * @since 2.6
 */
public class LinkedItemsIT
    extends NexusCoreITSupport
{

    private static final String AOP_POM = "aopalliance/aopalliance/1.0/aopalliance-1.0.pom";

    private static final String AOP_JAR = "aopalliance/aopalliance/1.0/aopalliance-1.0.jar";

    public LinkedItemsIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void linkToFileInSameHostedRepo()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploaded );

        linkedItems().create( repository.id(), "linkToAop.pom", AOP_POM );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository.id(), "linkToAop.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInSameHostedRepo()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploaded );

        linkedItems().create( repository.id(), "linkToAopDir", "aopalliance/aopalliance/1.0" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInSameHostedRepoEdingWithSlash()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploaded );

        linkedItems().create( repository.id(), "linkToAopDir", "aopalliance/aopalliance/1.0/" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToFileInOtherHostedRepo()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        linkedItems().create( repository1.id(), "linkToAop.pom", repository2.id(), AOP_POM );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAop.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInOtherHostedRepo()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        linkedItems().create( repository1.id(), "linkToAopDir", repository2.id(), "aopalliance/aopalliance/1.0" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInOtherHostedRepoEdingWithSlash()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        linkedItems().create( repository1.id(), "linkToAopDir", repository2.id(), "aopalliance/aopalliance/1.0/" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToFileInOtherProxyRepo()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        final MavenProxyRepository proxy = repositories()
            .create( MavenProxyRepository.class, repositoryIdForTest( "proxy" ) )
            .asProxyOf( repository2.contentUri() )
            .save();

        linkedItems().create( repository1.id(), "linkToAop.pom", proxy.id(), AOP_POM );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAop.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInOtherProxyRepo()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        final MavenProxyRepository proxy = repositories()
            .create( MavenProxyRepository.class, repositoryIdForTest( "proxy" ) )
            .asProxyOf( repository2.contentUri() )
            .save();

        linkedItems().create( repository1.id(), "linkToAopDir", proxy.id(), "aopalliance/aopalliance/1.0" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToDirInOtherProxyRepoEndingWithSlash()
        throws Exception
    {
        final MavenHostedRepository repository1 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "1" ) )
            .excludeFromSearchResults()
            .save();

        final MavenHostedRepository repository2 = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest( "2" ) )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository2.id(), AOP_POM ), uploaded );

        final MavenProxyRepository proxy = repositories()
            .create( MavenProxyRepository.class, repositoryIdForTest( "proxy" ) )
            .asProxyOf( repository2.contentUri() )
            .save();

        linkedItems().create( repository1.id(), "linkToAopDir", proxy.id(), "aopalliance/aopalliance/1.0/" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository1.id(), "linkToAopDir/aopalliance-1.0.pom" ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void linkToRootDirInSameHostedRepo()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploaded = testData().resolveFile( "artifacts/" + AOP_POM );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploaded );

        linkedItems().create( repository.id(), "linkToRootDir", "/" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAop.pom" );
        content().download( repositoryLocation( repository.id(), "linkToRootDir/" + AOP_POM ), downloaded );

        assertThat( downloaded, matchSha1( uploaded ) );
    }

    @Test
    public void listContentOfDir()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploadedPom = testData().resolveFile( "artifacts/" + AOP_POM );
        final File uploadedJar = testData().resolveFile( "artifacts/" + AOP_JAR );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploadedPom );
        content().upload( repositoryLocation( repository.id(), AOP_JAR ), uploadedJar );

        linkedItems().create( repository.id(), "linkToAopDir", "aopalliance/aopalliance/1.0" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAopDir" );
        content().download( repositoryLocation( repository.id(), "linkToAopDir/" ), downloaded );

        assertThat( downloaded, contains( "aopalliance-1.0.pom", "aopalliance-1.0.jar" ) );
    }

    @Test
    public void listContentOfDirEdingWithSlash()
        throws Exception
    {
        final MavenHostedRepository repository = repositories()
            .create( MavenHostedRepository.class, repositoryIdForTest() )
            .excludeFromSearchResults()
            .save();

        final File uploadedPom = testData().resolveFile( "artifacts/" + AOP_POM );
        final File uploadedJar = testData().resolveFile( "artifacts/" + AOP_JAR );
        content().upload( repositoryLocation( repository.id(), AOP_POM ), uploadedPom );
        content().upload( repositoryLocation( repository.id(), AOP_JAR ), uploadedJar );

        linkedItems().create( repository.id(), "linkToAopDir", "aopalliance/aopalliance/1.0/" );

        final File downloaded = new File( testIndex().getDirectory( "downloads" ), "linkToAopDir" );
        content().download( repositoryLocation( repository.id(), "linkToAopDir/" ), downloaded );

        assertThat( downloaded, contains( "aopalliance-1.0.pom", "aopalliance-1.0.jar" ) );
    }

    private LinkedItems linkedItems()
    {
        return client().getSubsystem( LinkedItems.class );
    }

}
