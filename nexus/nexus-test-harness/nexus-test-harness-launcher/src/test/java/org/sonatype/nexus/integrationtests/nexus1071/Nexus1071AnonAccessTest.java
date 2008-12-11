/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus1071;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * @author Juven Xu
 */
public class Nexus1071AnonAccessTest
    extends AbstractNexusIntegrationTest
{
    @Before
    public void turnOffSecure()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );
    }

    @After
    public void turnOnSecure()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void downloadArtifactFromPublicRepo()
        throws IOException
    {
        Gav gav = new Gav(
            this.getTestId(),
            "release-jar",
            "1",
            null,
            "jar",
            0,
            new Date().getTime(),
            "Release Jar",
            false,
            false,
            null,
            false,
            null );

        File artifact = this
            .downloadArtifactFromRepository( "nexus-test-harness-repo", gav, "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File originalFile = this.getTestResourceAsFile( "projects/" + gav.getArtifactId() + "/" + gav.getArtifactId()
            + "." + gav.getExtension() );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );

    }

    @Test
    public void downloadArtifactFromInternalRepo()
        throws IOException
    {
        Gav gav = new Gav(
            this.getTestId(),
            "release-jar-internal",
            "1",
            null,
            "jar",
            0,
            new Date().getTime(),
            "Release Jar Internal",
            false,
            false,
            null,
            false,
            null );
        try
        {
            downloadArtifactFromRepository( "Internal", gav, "./target/downloaded-jars" );

            Assert.fail( "Should throw 401 error" );
        }
        catch ( IOException e )
        {
            Assert.assertTrue( e.getMessage().contains( "401" ) );
        }

    }
}
