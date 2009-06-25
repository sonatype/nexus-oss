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
package org.sonatype.nexus.integrationtests.proxy.nexus177;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * Create three repositories, deploys a different artifact with the same name in each repo. Add each repo to a group
 * Access each repo and group, take one out of service. Access each repo and the group.
 */
public class Nexus177OutOfServiceTest
    extends AbstractNexusProxyIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public Nexus177OutOfServiceTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void outOfServiceTest()
        throws Exception
    {

        // get an artifact
        Gav gav =
            new Gav( this.getTestId(), "out-of-service", "0.1.8-four-beta18", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // download an artifact
        File originalFile = this.downloadArtifact( gav, "target/downloads/original" );

        // put proxy out of service
        this.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, true );

        // redownload artifact
        try
        {
            // download it
            downloadArtifact( gav, "./target/downloaded-jars" );
            Assert.fail( "Out Of Service Command didn't do anything." );
        }
        catch ( FileNotFoundException e )
        {
        }

        // put proxy back in service
        this.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false );

        // redownload artifact
        File newFile = this.downloadArtifact( gav, "target/downloads/original" );

        // compare the files just for kicks
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, newFile ) );

    }

}
