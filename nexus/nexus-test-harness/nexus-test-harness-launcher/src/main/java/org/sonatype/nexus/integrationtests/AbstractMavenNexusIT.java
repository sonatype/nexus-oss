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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;

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
        String log = FileUtils.fileRead( logFile );
        Assert.fail( log );
    }
}