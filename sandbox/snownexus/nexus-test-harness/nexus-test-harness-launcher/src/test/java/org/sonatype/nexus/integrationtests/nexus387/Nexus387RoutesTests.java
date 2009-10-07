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
package org.sonatype.nexus.integrationtests.nexus387;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

/**
 * Blocking, Exclusive, Inclusive Routes Tests
 */
public class Nexus387RoutesTests
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testExclusive()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".exclusive", "exclusive", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        try
        {
            // should fail
            this.downloadArtifactFromGroup( "exclusive-single", gav, "target/downloads/exclude" );
            Assert.fail( "Resource should not have been found." );
        }
        catch ( IOException e )
        {
        }

        File artifact = this.downloadArtifactFromGroup( "exclusive-group", gav, "target/downloads/exclude" );
        Assert.assertNotNull( artifact );

        String line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + this.getFirstLineOfFile( artifact ) + ", expected: exclusive2",
                             "exclusive2", line );

        artifact = this.downloadArtifactFromGroup( "other-group", gav, "target/downloads/exclude" );
        Assert.assertNotNull( artifact );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + line + ", expected: exclusive1", "exclusive1", line );

    }

    @Test
    public void testInclusive()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".inclusive", "inclusive", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        File artifact = this.downloadArtifactFromGroup( "inclusive-single", gav, "target/downloads/include" );

        String line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + this.getFirstLineOfFile( artifact ) + ", expected: inclusive1",
                             "inclusive1", line );

        artifact = this.downloadArtifactFromGroup( "inclusive-group", gav, "target/downloads/include" );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + this.getFirstLineOfFile( artifact ) + ", expected: inclusive2",
                             "inclusive2", line );

        artifact = this.downloadArtifactFromGroup( "other-group", gav, "target/downloads/include" );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + this.getFirstLineOfFile( artifact ) + ", expected: inclusive1",
                             "inclusive1", line );

    }

    @Test
    public void testBlocking()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".blocking", "blocking", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        try
        {

            this.downloadArtifactFromGroup( "blocking-group", gav, "target/downloads/blocking" );
            Assert.fail( "This file should not have been found." );

        }
        catch ( IOException e )
        {
        }
        File artifact = this.downloadArtifactFromGroup( "other-group", gav, "target/downloads/blocking" );

        String line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( "Jar contained: " + this.getFirstLineOfFile( artifact ) + ", expected: blocking1",
                             "blocking1", line );

    }

    private String getFirstLineOfFile( File file )
        throws IOException
    {
        BufferedReader bReader = new BufferedReader( new FileReader( file ) );
        String line = bReader.readLine().trim(); // only need one line
        bReader.close();

        return line;

    }

}
