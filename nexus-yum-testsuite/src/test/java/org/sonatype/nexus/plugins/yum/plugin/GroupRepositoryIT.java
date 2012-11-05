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
package org.sonatype.nexus.plugins.yum.plugin;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.plugins.yum.RepoUtil.createGroupRepository;
import static org.sonatype.nexus.plugins.yum.RepoUtil.createHostedRepo;
import static org.sonatype.nexus.plugins.yum.RepoUtil.memberRepo;
import static org.sonatype.nexus.plugins.yum.TimeUtil.sleep;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.MetadataType.PRIMARY_XML;

import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.artifact.MavenArtifact;
import org.sonatype.nexus.client.core.subsystem.artifact.UploadRequest;
import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;
import org.sonatype.nexus.test.os.IgnoreOn;
import org.sonatype.nexus.test.os.OsTestRule;

public class GroupRepositoryIT
    extends AbstractIntegrationTestCase
{
    @Rule
    public OsTestRule osTestRule = new OsTestRule();

    @Test
    public void shouldCreateAGroupRepository()
        throws Exception
    {
        final Repositories repositories = client().getSubsystem( Repositories.class );
        final GroupRepository groupRepo = createGroupRepository( repositories, "maven2yum" );
        assertThat( groupRepo.settings().getProvider(), is( "maven2yum" ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateRepoAfterUpload()
        throws Exception
    {
        final GroupRepository groupRepo = givenGroupRepoWith2Rpms();
        final String primaryXml = getPrimaryXml( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateGroupRepoWhenMemberRepoIsRemoved()
        throws Exception
    {
        final GroupRepository groupRepo = givenGroupRepoWith2Rpms();
        groupRepo.settings().getRepositories().remove( 1 );
        groupRepo.save();
        sleep( 5, SECONDS );
        final String primaryXml = getPrimaryXml( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, not( containsString( "test-rpm" ) ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateGroupRepoWhenMemberRepoIsAdded()
        throws Exception
    {
        final GroupRepository groupRepo = givenGroupRepoWith2Rpms();
        final String repo3 = createHostedRepo( client() ).getId();
        final MavenArtifact artifact = client().getSubsystem( MavenArtifact.class );
        artifact.upload( new UploadRequest( repo3, "a_group3", "an_artifact3", "3.0", "pom", "", "rpm",
            testData( "rpm/foo-bar-5.1.2-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        groupRepo.settings().getRepositories().add( memberRepo( repo3 ) );
        groupRepo.save();
        sleep( 5, SECONDS );
        final String primaryXml = getPrimaryXml( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
        assertThat( primaryXml, containsString( "foo-bar" ) );
    }

    private String getPrimaryXml( final GroupRepository groupRepo )
    {
        final YumClient yum = client().getSubsystem( YumClient.class );
        final String primaryXml = yum.getGroupMetadata( groupRepo.settings().getId(), PRIMARY_XML, String.class );
        return primaryXml;
    }

    private GroupRepository givenGroupRepoWith2Rpms()
        throws InterruptedException, URISyntaxException
    {
        final String repo1 = createHostedRepo( client() ).getId();
        final String repo2 = createHostedRepo( client() ).getId();
        final Repositories repositories = client().getSubsystem( Repositories.class );
        final GroupRepository groupRepo = createGroupRepository( repositories, "maven2yum", repo1, repo2 );
        sleep( 5, SECONDS );
        final MavenArtifact artifact = client().getSubsystem( MavenArtifact.class );
        artifact.upload( new UploadRequest( repo1, "a_group1", "an_artifact1", "1.0", "pom", "", "rpm",
            testData( "rpm/test-artifact-1.2.3-1.noarch.rpm" ) ) );
        artifact.upload( new UploadRequest( repo2, "a_group2", "an_artifact2", "2.0", "pom", "", "rpm",
            testData( "rpm/test-rpm-5.6.7-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        return groupRepo;
    }

}
