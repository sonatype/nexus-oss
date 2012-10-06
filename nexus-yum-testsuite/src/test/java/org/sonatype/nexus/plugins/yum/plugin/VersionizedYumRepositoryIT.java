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
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.plugins.yum.RepoUtil.createHostedRepo;
import static org.sonatype.nexus.plugins.yum.TimeUtil.sleep;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.MetadataType.INDEX;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.MetadataType.PRIMARY_XML;

import java.net.URISyntaxException;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.artifact.MavenArtifact;
import org.sonatype.nexus.client.core.subsystem.artifact.UploadRequest;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;

public class VersionizedYumRepositoryIT
    extends AbstractIntegrationTestCase
{
    private static final String VERSION = "1.0";

    private static final String ARTIFACT_ID = "artifact";

    private static final String GROUP_ID = "group";

    private static final String ALIAS = "alias";

    @Test
    public void shouldGenerateVersionizedRepoForVersion()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final YumClient yum = client().getSubsystem( YumClient.class );
        final String content = yum.getMetadata( repoName, VERSION, PRIMARY_XML, String.class );
        assertThat( content, containsString( "test-artifact" ) );
    }

    @Test
    public void shouldGenerateVersionizedRepoForAlias()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final YumClient yum = client().getSubsystem( YumClient.class );
        yum.createOrUpdateAlias( repoName, ALIAS, VERSION );
        final String content = yum.getMetadata( repoName, ALIAS, PRIMARY_XML, String.class );
        assertThat( content, containsString( "test-artifact" ) );
    }

    @Test
    public void shouldGenerateIndexHtml()
        throws Exception
    {
        final String repoName = givenRepositoryWithRpm();
        final YumClient yum = client().getSubsystem( YumClient.class );
        final String content = yum.getMetadata( repoName, VERSION, INDEX, String.class );
        assertThat( content, containsString( "<a href=\"repodata/\">repodata/</a>" ) );
    }

    private String givenRepositoryWithRpm()
        throws URISyntaxException, InterruptedException
    {
        final String repoName = createHostedRepo( client() ).getId();
        final MavenArtifact artifact = client().getSubsystem( MavenArtifact.class );
        artifact.upload( new UploadRequest( repoName, GROUP_ID, ARTIFACT_ID, VERSION, "pom", "", "rpm",
            resource( "/test-artifact-1.2.3-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        return repoName;
    }
}
