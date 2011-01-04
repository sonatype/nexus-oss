/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.webproxy.nexus1146;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1146RepositoryOverProxyIT
    extends AbstractNexusWebProxyIntegrationTest
{
    @Test(groups = PROXY)
    public void downloadArtifactOverWebProxy()
        throws Exception
    {
        File pomFile = this.getLocalFile( "release-proxy-repo-1", getTestId(), "artifact", "1.0", "pom" );
        File pomArtifact = this.downloadArtifact( getTestId(), "artifact", "1.0", "pom", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( pomArtifact, pomFile ) );

        File jarFile = this.getLocalFile( "release-proxy-repo-1", getTestId(), "artifact", "1.0", "jar" );
        File jarArtifact = this.downloadArtifact( getTestId(), "artifact", "1.0", "jar", null, "target/downloads" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( jarArtifact, jarFile ) );

        String artifactUrl = baseProxyURL + "release-proxy-repo-1/" + getTestId() + "/artifact/1.0/artifact-1.0.jar";
        Assert.assertTrue( server.getAccessedUris().contains( artifactUrl ), "Proxy was not accessed" );
    }

    @Test(groups = PROXY, expectedExceptions = FileNotFoundException.class )
    public void unexistentArtifact()
        throws Exception
    {
        try
        {
            this.downloadArtifact( getTestId(), "some-artifact-that-dont-exists", "4.8.15.16.23.42", "jar", null,
                                   "target/downloads" );
        }
        finally
        {
            String artifactUrl =
                baseProxyURL
                    + "release-proxy-repo-1/"
                    + getTestId()
                    + "/some-artifact-that-dont-exists/4.8.15.16.23.42/some-artifact-that-dont-exists-4.8.15.16.23.42.jar";
            Assert.assertTrue( server.getAccessedUris().contains( artifactUrl ), "Proxy was not accessed" );
        }
    }

    @Test(groups = PROXY)
    public void proxyWithMaven()
        throws Exception
    {
        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );

        File mavenProject = getTestFile( "pom.xml" ).getParentFile();
        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );

        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        options.add( "-s " + getOverridableFile( "settings.xml" ) );
        verifier.setCliOptions( options );

        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();

        String artifactUrl =
            baseProxyURL + "release-proxy-repo-1/" + getTestId() + "/maven-artifact/1.0/maven-artifact-1.0.jar";
        Assert.assertTrue( server.getAccessedUris().contains( artifactUrl ), "Proxy was not accessed" );
    }

}
