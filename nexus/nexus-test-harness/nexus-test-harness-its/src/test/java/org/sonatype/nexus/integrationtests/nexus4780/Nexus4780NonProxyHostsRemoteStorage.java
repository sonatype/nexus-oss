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
package org.sonatype.nexus.integrationtests.nexus4780;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;

import org.sonatype.nexus.integrationtests.ITGroups;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class Nexus4780NonProxyHostsRemoteStorage
    extends AbstractNexusWebProxyIntegrationTest
{

    @Test( groups = ITGroups.PROXY )
    public void apache3xDownloadArtifactNonProxyHost()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus4780", "artifact", "1.0", "pom" );
        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus4780", "artifact", "1.0", "jar" );

        final String repoUrl = getNexusTestRepoUrl( "apache3x" );
        File pomArtifact = this.downloadArtifact( repoUrl, "nexus4780", "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarArtifact = this.downloadArtifact( repoUrl, "nexus4780", "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        assertThat( "proxy was used" ,server.getAccessedUris(), hasSize( 0 ) );
    }

    @Test( groups = ITGroups.PROXY )
    public void apache4xDownloadArtifactNonProxyHost()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", "nexus4780", "artifact", "1.0", "pom" );
        File jarFile = this.getLocalFile( "release-proxy-repo-1", "nexus4780", "artifact", "1.0", "jar" );

        final String repoUrl = getNexusTestRepoUrl( "apache4x" );
        File pomArtifact = this.downloadArtifact( repoUrl, "nexus4780", "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarArtifact = this.downloadArtifact( repoUrl, "nexus4780", "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        assertThat( "proxy was used" ,server.getAccessedUris(), hasSize( 0 ) );
    }
}
