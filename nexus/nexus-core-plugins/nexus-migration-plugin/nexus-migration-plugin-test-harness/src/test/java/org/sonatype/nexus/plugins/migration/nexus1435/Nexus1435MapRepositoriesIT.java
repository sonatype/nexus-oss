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
package org.sonatype.nexus.plugins.migration.nexus1435;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1435MapRepositoriesIT
    extends AbstractMigrationIntegrationTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void downloadMixedRepo()
        throws Exception
    {

        File artifact = getTestFile( "artifact.jar" );
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/main-local/nxcm255/released/1.0/released-1.0.jar" );
        File downloaded;
        try
        {
            downloaded = this.downloadFile( url, "target/downloads/nxcm255" );
        }
        catch ( IOException e )
        {
            Assert.fail( "Unable to download artifact " + url + " got:\n" + e.getMessage() );
            throw e; // never happen
        }

        Assert.assertTrue( "Downloaded artifact was not right, checksum comparation fail " + url,
                           FileTestingUtils.compareFileSHA1s( artifact, downloaded ) );

    }

    @Test
    public void downloadMixedRepoSnapshot()
    throws Exception
    {

        File artifact = getTestFile( "artifact.jar" );
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                     + "/artifactory/main-local/nxcm255/snapshot/1.0-SNAPSHOT/snapshot-1.0-SNAPSHOT.jar" );
        File downloaded;
        try
        {
            downloaded = this.downloadFile( url, "target/downloads/nxcm255" );
        }
        catch ( IOException e )
        {
            Assert.fail( "Unable to download artifact " + url + " got:\n" + e.getMessage() );
            throw e; // never happen
        }

        Assert.assertTrue( "Downloaded artifact was not right, checksum comparation fail " + url,
                           FileTestingUtils.compareFileSHA1s( artifact, downloaded ) );

    }

    @Test
    public void resolveMixedRepo() throws Exception {
        File mavenProject = getTestFile( "maven-project" );

        Verifier verifier = createVerifier( mavenProject, null );
        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();
    }

    /**
     * Create a nexus verifier instance
     *
     * @param mavenProject Maven Project folder
     * @param settings A settings.xml file
     * @return
     * @throws VerificationException
     * @throws IOException
     */
    public Verifier createVerifier( File mavenProject, File settings )
        throws VerificationException, IOException
    {
        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );
        
        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );

        String logname = "logs/maven-execution/" + getTestId() + "/" + mavenProject.getName() + ".log";
        new File( verifier.getBasedir(), logname ).getParentFile().mkdirs();
        verifier.setLogFileName( logname );
        
        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );
        cleanRepository( mavenRepository );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-U" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        if ( settings != null )
        {
            options.add( "-s " + settings.getAbsolutePath() );
        }
        else
        {
            options.add( "-s " + this.getOverridableFile( "settings.xml" ) );
        }
        verifier.setCliOptions( options );
        return verifier;
    }

    /**
     * Remove all artifacts on <code>testId</code> groupId
     *
     * @param verifier
     * @throws IOException
     */
    public void cleanRepository( File mavenRepo )
        throws IOException
    {

        File testGroupIdFolder = new File( mavenRepo, getTestId() );
        FileUtils.deleteDirectory( testGroupIdFolder );

    }
}
