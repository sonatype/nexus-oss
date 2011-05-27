/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.obr.test.nxcm858;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.testng.annotations.Test;

import com.sonatype.nexus.plugin.obr.test.AbstractOBRIntegrationTest;

public class NXCM858DeployToHostedObrRepoIT
    extends AbstractOBRIntegrationTest
{

    @Test
    public void deployToHosted()
        throws Exception
    {
        File project = getTestFile( "helloworld-hs" );
        File s = getOverridableFile( "settings.xml" );

        Verifier v = AbstractMavenNexusIT.createMavenVerifier( project, s, getTestId() );

        v.executeGoal( "deploy" );

        v.verifyErrorFreeLog();
    }
}
