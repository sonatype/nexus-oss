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
package org.sonatype.nexus.integrationtests.nexus1071;

import static org.sonatype.nexus.integrationtests.ITGroups.SECURITY;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Juven Xu
 */
public class Nexus1071DeployToRepoAnonCannotAccessIT
    extends AbstractMavenNexusIT
{
    @BeforeClass(alwaysRun = true)
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test(groups = SECURITY)
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

    @Test(groups = SECURITY)
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

    @Test(groups = SECURITY)
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

    @Test(groups = SECURITY)
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
