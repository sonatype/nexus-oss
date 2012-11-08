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
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.repository.yum.client.MetadataType.INDEX;
import static org.sonatype.nexus.repository.yum.client.MetadataType.PRIMARY_XML;

import java.net.URISyntaxException;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.artifact.UploadRequest;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.repository.yum.client.Yum;

public class VersionedYumRepositoryIT
    extends YumRepositoryITSupport
{

    private static final String VERSION = "1.0";

    private static final String ARTIFACT_ID = "artifact";

    private static final String GROUP_ID = "group";

    private static final String ALIAS = "alias";

    @Test
    public void shouldGenerateVersionedRepoForVersion()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final Yum yum = client().getSubsystem( Yum.class );
        final String content = yum.getMetadata( repoName, VERSION, PRIMARY_XML, String.class );
        assertThat( content, containsString( "test-artifact" ) );
    }

    @Test
    public void shouldGenerateVersionedRepoForAlias()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final Yum yum = client().getSubsystem( Yum.class );
        yum.createOrUpdateAlias( repoName, ALIAS, VERSION );
        final String content = yum.getMetadata( repoName, ALIAS, PRIMARY_XML, String.class );
        assertThat( content, containsString( "test-artifact" ) );
    }

    @Test
    public void shouldGenerateIndexHtml()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final Yum yum = client().getSubsystem( Yum.class );
        final String content = yum.getMetadata( repoName, VERSION, INDEX, String.class );
        assertThat( content, containsString( "<a href=\"repodata/\">repodata/</a>" ) );
    }

    private String givenRepositoryWithRpm()
        throws URISyntaxException, InterruptedException
    {
        final MavenHostedRepository repository = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest()
        ).save();

        mavenArtifact().upload(
            new UploadRequest(
                repository.id(), GROUP_ID, ARTIFACT_ID, VERSION, "pom", "", "rpm",
                testData.resolveFile( "/rpms/test-artifact-1.2.3-1.noarch.rpm" )
            )
        );
        sleep( 5, SECONDS );

        return repository.id();
    }
}
