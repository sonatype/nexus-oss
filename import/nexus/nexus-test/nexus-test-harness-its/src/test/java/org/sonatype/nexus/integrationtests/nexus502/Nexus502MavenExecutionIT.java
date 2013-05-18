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
package org.sonatype.nexus.integrationtests.nexus502;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.security.rest.model.UserResource;

/**
 * Put a bunch of artifacts in a repo, and then run a maven project to download them
 */
public class Nexus502MavenExecutionIT
    extends AbstractMavenNexusIT
{

    private Verifier verifier;

    @BeforeClass
    public static void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Before
    public void createVerifier()
        throws Exception
    {
        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "repositories.xml" );
        verifier = createVerifier( mavenProject, settings );
    }

    @Test
    public void dependencyDownload()
        throws Exception
    {
        try
        {
            verifier.executeGoal( "dependency:resolve" );
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest( verifier );
        }
    }

    @Test //( dependsOnMethods = { "dependencyDownload" } )
    public void dependencyDownloadPrivateServer()
        throws Exception
    {
        // Disable anonymous
        disableUser( "anonymous" );

        try
        {
            verifier.executeGoal( "dependency:resolve" );
            verifier.verifyErrorFreeLog();
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // Expected exception
        }
    }

    private UserResource disableUser( String userId )
        throws IOException
    {
        UserMessageUtil util = new UserMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        return util.disableUser( userId );
    }

    // Depends on nexus-508
    @Test //( dependsOnMethods = { "dependencyDownloadPrivateServer" } )
    public void dependencyDownloadProtectedServer()
        throws Exception
    {
        // Disable anonymous
        disableUser( "anonymous" );

        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "repositoriesWithAuthentication.xml" );

        Verifier verifier = createVerifier( mavenProject, settings );
        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();
    }

}
