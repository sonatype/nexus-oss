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
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.test.os.IgnoreOn;
import org.sonatype.nexus.test.os.OsTestRule;

public class MergeMetadataIT
    extends YumITSupport
{

    @Rule
    public OsTestRule osTestRule = new OsTestRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public MergeMetadataIT( final String nexusBundleCoordinates )
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

        waitForNexusToSettleDown();

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

        waitForNexusToSettleDown();

        thrown.expect( NexusClientNotFoundException.class );
        getPrimaryXmlOf( groupRepo );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldRegenerateGroupRepoWhenMemberRepoIsAdded()
        throws Exception
    {
        final GroupRepository groupRepo = givenAYumGroupRepoWith2RPMs();

        final Repository repo3 = createYumEnabledRepository( repositoryIdForTest( "3" ) );

        content().upload(
            repositoryLocation( repo3.id(), "a_group3/an_artifact3/3.0/an_artifact3-3.0.rpm" ),
            testData().resolveFile( "/rpms/foo-bar-5.1.2-1.noarch.rpm" )
        );

        waitForNexusToSettleDown();

        groupRepo.addMember( repo3.id() ).save();

        waitForNexusToSettleDown();

        final String primaryXml = getPrimaryXmlOf( groupRepo );

        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
        assertThat( primaryXml, containsString( "foo-bar" ) );
    }

    @Test
    @IgnoreOn( "mac" )
    public void shouldIncludeProxyRepository()
        throws Exception
    {
        final Repository repo1 = createYumEnabledRepository( repositoryIdForTest( "1" ) );
        final Repository repo2 = createYumEnabledRepository( repositoryIdForTest( "2" ) );

        final Repository proxyRepo = repositories()
            .create( MavenProxyRepository.class, repositoryIdForTest( "proxy" ) )
            .asProxyOf( repo1.contentUri() )
            .save();

        final GroupRepository groupRepo = createYumEnabledGroupRepository(
            repositoryIdForTest(), repo2.id(), proxyRepo.id()
        );

        content().upload(
            repositoryLocation( repo1.id(), "a_group1/an_artifact1/1.0/an_artifact1-1.0.rpm" ),
            testData().resolveFile( "/rpms/test-artifact-1.2.3-1.noarch.rpm" )
        );

        content().upload(
            repositoryLocation( repo2.id(), "a_group2/an_artifact2/2.0/an_artifact2-2.0.rpm" ),
            testData.resolveFile( "/rpms/test-rpm-5.6.7-1.noarch.rpm" )
        );

        waitForNexusToSettleDown();

        final String primaryXml = getPrimaryXmlOf( groupRepo );
        assertThat( primaryXml, containsString( "test-artifact" ) );
        assertThat( primaryXml, containsString( "test-rpm" ) );
    }

    private String getPrimaryXmlOf( final GroupRepository groupRepo )
        throws IOException
    {
        return yum().getMetadata( groupRepo.id(), PRIMARY_XML, String.class );
    }

    private GroupRepository givenAYumGroupRepoWith2RPMs()
        throws Exception
    {
        final Repository repo1 = createYumEnabledRepository( repositoryIdForTest( "1" ) );
        final Repository repo2 = createYumEnabledRepository( repositoryIdForTest( "2" ) );
        final Repository repoX = createYumEnabledRepository( repositoryIdForTest( "X" ) );

        final GroupRepository groupRepo = createYumEnabledGroupRepository(
            repositoryIdForTest(), repo1.id(), repo2.id(), repoX.id()
        );

        content().upload(
            repositoryLocation( repo1.id(), "a_group1/an_artifact1/1.0/an_artifact1-1.0.rpm" ),
            testData().resolveFile( "/rpms/test-artifact-1.2.3-1.noarch.rpm" )
        );

        content().upload(
            repositoryLocation( repo2.id(), "a_group2/an_artifact2/2.0/an_artifact2-2.0.rpm" ),
            testData.resolveFile( "/rpms/test-rpm-5.6.7-1.noarch.rpm" )
        );

        waitForNexusToSettleDown();

        return groupRepo;
    }

}
