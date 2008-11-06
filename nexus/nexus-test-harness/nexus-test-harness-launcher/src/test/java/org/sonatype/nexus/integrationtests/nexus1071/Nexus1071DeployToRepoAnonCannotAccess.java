/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

package org.sonatype.nexus.integrationtests.nexus1071;

import java.io.File;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus1071DeployToRepoAnonCannotAccess
    extends AbstractMavenNexusIT
{
    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void deployRepeatly()
        throws Exception
    {
        File mavenProject1 = getTestFile( "maven-project-1" );

        File settings1 = getTestFile( "settings1.xml" );

        Verifier verifier1 = null;

        try
        {
            verifier1 = createVerifier( mavenProject1, settings1 );

            verifier1.executeGoal( "deploy" );

            verifier1.verifyErrorFreeLog();
        }

        catch ( VerificationException e )
        {
            failTest( verifier1 );
        }

        try
        {
            verifier1.executeGoal( "deploy" );

            verifier1.verifyErrorFreeLog();

            Assert.fail( "Should return 401 error" );
        }
        catch ( VerificationException e )
        {
            // 401 error
        }
    }

    @Test
    public void deploySnapshot()
        throws Exception
    {
        File mavenProject = getTestFile( "maven-project-snapshot" );

        File settings = getTestFile( "settings-snapshot.xml" );

        Verifier verifier = null;

        try
        {
            verifier = createVerifier( mavenProject, settings );

            verifier.executeGoal( "deploy" );

            verifier.verifyErrorFreeLog();
        }

        catch ( VerificationException e )
        {
            failTest( verifier );
        }
    }

    @Test
    public void deployToAnotherRepo()
        throws Exception
    {

        File mavenProject2 = getTestFile( "maven-project-2" );

        File settings2 = getTestFile( "settings2.xml" );

        Verifier verifier2 = null;

        try
        {
            verifier2 = createVerifier( mavenProject2, settings2 );

            verifier2.executeGoal( "deploy" );

            verifier2.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest( verifier2 );
        }
    }
    
    @Test
    public void anonDeploy()
        throws Exception
    {
        File mavenProjectAnon = getTestFile( "maven-project-anon" );

        File settingsAnon = getTestFile( "settings-anon.xml" );

        Verifier verifierAnon = null;

        try
        {
            verifierAnon = createVerifier( mavenProjectAnon, settingsAnon );

            verifierAnon.executeGoal( "deploy" );

            verifierAnon.verifyErrorFreeLog();

            Assert.fail( "Should return 401 error" );
        }
        catch ( VerificationException e )
        {
            // test pass
        }
    }

}
