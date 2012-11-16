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
package org.sonatype.nexus.repository.yum.testsuite;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation;
import static org.sonatype.nexus.repository.yum.client.MetadataType.PRIMARY_XML;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.repository.yum.client.YumGroupRepository;
import org.sonatype.nexus.test.os.IgnoreOn;
import org.sonatype.nexus.test.os.OsTestRule;

public class YumGroupRepositoryIT
    extends YumRepositoryITSupport
{

    @Rule
    public OsTestRule osTestRule = new OsTestRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public YumGroupRepositoryIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateRepoAfterUpload()
        throws Exception
    {
        final GroupRepository groupRepo = givenAYumGroupRepoWith2RPMs();

        final String primaryXml = getPrimaryXmlOf( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateGroupRepoWhenMemberRepoIsRemoved()
        throws Exception
    {
        final GroupRepository groupRepo = givenAYumGroupRepoWith2RPMs();
        groupRepo.removeMember( repositoryIdForTest( "2" ) ).save();

        sleep( 5, SECONDS );

        final String primaryXml = getPrimaryXmlOf( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, not( containsString( "test-rpm" ) ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void removeYumRepositoryWhenOnlyOneMember()
        throws Exception
    {
        final GroupRepository groupRepo = givenAYumGroupRepoWith2RPMs();
        groupRepo.removeMember( repositoryIdForTest( "1" ) ).save();
        groupRepo.removeMember( repositoryIdForTest( "2" ) ).save();

        sleep( 5, SECONDS );

        thrown.expect( NexusClientNotFoundException.class );
        getPrimaryXmlOf( groupRepo );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateGroupRepoWhenMemberRepoIsAdded()
        throws Exception
    {
        final GroupRepository groupRepo = givenAYumGroupRepoWith2RPMs();

        final MavenHostedRepository repo3 = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest( "3" )
        ).excludeFromSearchResults().save();

        content().upload(
            repositoryLocation( repo3.id(), "a_group3/an_artifact3/3.0/an_artifact3-3.0.rpm" ),
            testData().resolveFile( "/rpms/foo-bar-5.1.2-1.noarch.rpm" )
        );

        sleep( 5, SECONDS );

        groupRepo.addMember( repo3.id() ).save();

        sleep( 5, SECONDS );

        final String primaryXml = getPrimaryXmlOf( groupRepo );

        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
        assertThat( primaryXml, containsString( "foo-bar" ) );
    }

    private String getPrimaryXmlOf( final GroupRepository groupRepo )
        throws IOException
    {
        return yum().getMetadata( groupRepo.id(), PRIMARY_XML, String.class );
    }

    private YumGroupRepository givenAYumGroupRepoWith2RPMs()
        throws Exception
    {
        final MavenHostedRepository repo1 = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest( "1" )
        ).excludeFromSearchResults().save();

        final MavenHostedRepository repo2 = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest( "2" )
        ).excludeFromSearchResults().save();

        final MavenHostedRepository repoX = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest( "X" )
        ).excludeFromSearchResults().save();

        final YumGroupRepository groupRepo = repositories().create( YumGroupRepository.class, repositoryIdForTest() )
            .ofRepositories( repo1.id(), repo2.id(), repoX.id() )
            .save();

        sleep( 5, SECONDS );

        content().upload(
            repositoryLocation( repo1.id(), "a_group1/an_artifact1/1.0/an_artifact1-1.0.rpm" ),
            testData().resolveFile( "/rpms/test-artifact-1.2.3-1.noarch.rpm" )
        );

        content().upload(
            repositoryLocation( repo2.id(), "a_group2/an_artifact2/2.0/an_artifact2-2.0.rpm" ),
            testData.resolveFile( "/rpms/test-rpm-5.6.7-1.noarch.rpm" )
        );

        sleep( 5, SECONDS );

        return groupRepo;
    }

}
