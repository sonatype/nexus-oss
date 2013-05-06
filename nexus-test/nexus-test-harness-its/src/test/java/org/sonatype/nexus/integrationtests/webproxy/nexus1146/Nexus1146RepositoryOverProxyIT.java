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
package org.sonatype.nexus.integrationtests.webproxy.nexus1146;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.sonatype.nexus.integrationtests.ITGroups.PROXY;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1146RepositoryOverProxyIT
    extends AbstractNexusWebProxyIntegrationTest
{
    @Test @Category( PROXY.class )
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
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }

    @Test( expected = FileNotFoundException.class ) @Category( PROXY.class )
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
            Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
        }
    }

    @Test @Category( PROXY.class )
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
        Assert.assertTrue( "Proxy was not accessed", server.getAccessedUris().contains( artifactUrl ) );
    }

}
