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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.nexus.test.utils.TestProperties;

public class AbstractMavenNexusIT
    extends AbstractNexusIntegrationTest
{

    public AbstractMavenNexusIT()
    {
        super();
    }

    public AbstractMavenNexusIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    public Verifier createVerifier( File mavenProject )
        throws VerificationException, IOException
    {
        return createVerifier( mavenProject, null );
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
        if ( settings == null )
        {
            settings = getOverridableFile( "settings.xml" );
        }
        return createMavenVerifier( mavenProject, settings, getTestId() );
    }

    public static Verifier createMavenVerifier( File mavenProject, File settings, String testId )
        throws VerificationException, IOException
    {
        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );

        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );

        String logname = "logs/maven-execution/" + testId + "/" + mavenProject.getName() + ".log";
        new File( verifier.getBasedir(), logname ).getParentFile().mkdirs();
        verifier.setLogFileName( logname );

        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );
        cleanRepository( mavenRepository, testId );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        options.add( "-s " + settings.getAbsolutePath() );
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

        cleanRepository( mavenRepo, getTestId() );
    }

    /**
     * Remove all artifacts on <code>testId</code> groupId
     * 
     * @param verifier
     * @throws IOException
     */
    public static void cleanRepository( File mavenRepo, String testId )
        throws IOException
    {

        File testGroupIdFolder = new File( mavenRepo, testId );
        FileUtils.deleteDirectory( testGroupIdFolder );

    }

    /**
     * Workaround to get some decent logging when tests fail
     * 
     * @throws IOException
     */
    protected void failTest( Verifier verifier )
        throws IOException
    {
        File logFile = new File( verifier.getBasedir(), verifier.getLogFileName() );
        String log = FileUtils.readFileToString( logFile );
        Assert.fail( log );
    }
}