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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
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

    private Verifier verifier;

    @Before
    public void createVerifier()
        throws Exception
    {
        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "server.xml" );
        verifier = createVerifier( mavenProject, settings );
    }

    @Test
    public void deploy()
        throws Exception
    {
        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest( verifier );
        }
    }
}
