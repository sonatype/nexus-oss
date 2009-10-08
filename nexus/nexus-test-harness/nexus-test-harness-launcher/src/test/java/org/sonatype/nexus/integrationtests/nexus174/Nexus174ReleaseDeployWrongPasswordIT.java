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
package org.sonatype.nexus.integrationtests.nexus174;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.test.utils.MavenDeployer;

/**
 * Test deploying artifacts using the wrong password. (expected to fail)
 */
public class Nexus174ReleaseDeployWrongPasswordIT
    extends AbstractPrivilegeTest
{

    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus174ReleaseDeployWrongPasswordIT()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void deployWithMaven() throws Exception
    {

        // GAV
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // we need to delete the files...
        this.deleteFromRepository( this.getTestId() + "/" );

        try
        {
            // DeployUtils.forkDeployWithWagon( this.getContainer(), "http", this.getNexusTestRepoUrl(), fileToDeploy,
            // this.getRelitiveArtifactPath( gav ));
            MavenDeployer.deploy( gav, this.getNexusTestRepoUrl(), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
            Assert.fail( "File should NOT have been deployed" );
        }
        // catch ( TransferFailedException e )
        // {
        // // expected 401
        // }
        catch ( CommandLineException e )
        {
            // expected 401
            // MavenDeployer, either fails or not, we can't check the cause of the problem
        }

    }


}
