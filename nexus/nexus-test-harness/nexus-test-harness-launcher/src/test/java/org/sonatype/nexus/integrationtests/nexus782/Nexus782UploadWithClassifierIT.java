/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus782;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus782UploadWithClassifierIT
    extends AbstractNexusIntegrationTest
{
    
    private Gav artifactGav = new Gav(
        this.getTestId(),
        "same-pom",
        "1.2.5",
        null,
        "jar",
        0,
        new Date().getTime(),
        "same-pom",
        false,
        false,
        null,
        false,
        null );

    private Gav artifactClassifierGav = new Gav(
        this.getTestId(),
        "same-pom",
        "1.2.5",
        "CLASSIFIER",
        "jar",
        0,
        new Date().getTime(),
        "same-pom",
        false,
        false,
        null,
        false,
        null );

    public Nexus782UploadWithClassifierIT()
        throws Exception
    {
    }

    @Test
    public void withSamePomNoExtention()
        throws HttpException,
            IOException
    {

        File artifactFile = this.getTestFile( "same-pom/same-pom.jar" );
        File artifactClassifierFile = this.getTestFile( "same-pom/same-pom-classifier.jar" );
        File pomFile = this.getTestFile( "same-pom/pom.xml" );

        // upload jar artifact
        DeployUtils.deployUsingPomWithRest( this.getTestRepositoryId(), artifactFile, pomFile, null, null );

        // make sure everything is cool so far
        this.checkUpload( this.artifactGav, artifactFile );

        // upload jar artifact with classifier
        DeployUtils.deployUsingPomWithRest( this.getTestRepositoryId(), artifactClassifierFile, pomFile, this.artifactClassifierGav.getClassifier(), null );

        // now check files again
        this.checkUpload( this.artifactGav, artifactFile );
        this.checkUpload( this.artifactClassifierGav, artifactClassifierFile );
        
    }
    
    @Test
    public void withSamePomExtention()
        throws HttpException,
            IOException
    {

        File artifactFile = this.getTestFile( "same-pom/same-pom.jar" );
        File artifactClassifierFile = this.getTestFile( "same-pom/same-pom-classifier.jar" );
        File pomFile = this.getTestFile( "same-pom/pom.xml" );

        // upload jar artifact
        DeployUtils.deployUsingPomWithRest( this.getTestRepositoryId(), artifactFile, pomFile, null, this.artifactGav.getExtension() );

        // make sure everything is cool so far
        this.checkUpload( this.artifactGav, artifactFile );

        // upload jar artifact with classifier
        DeployUtils.deployUsingPomWithRest( this.getTestRepositoryId(), artifactClassifierFile, pomFile, this.artifactClassifierGav.getClassifier(), this.artifactClassifierGav.getExtension() );

        // now check files again
        this.checkUpload( this.artifactGav, artifactFile );
        this.checkUpload( this.artifactClassifierGav, artifactClassifierFile );
        
    }

    private void checkUpload( Gav gav, File originalFile )
        throws IOException
    {
        // download it
        File artifact = downloadArtifact( gav, "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );
    }

}
