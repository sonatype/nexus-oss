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
package org.sonatype.nexus.integrationtests.nexus387;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Blocking, Exclusive, Inclusive Routes Tests
 */
public class Nexus387RoutesIT
    extends AbstractNexusIntegrationTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testExclusive()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".exclusive", "exclusive", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, null, false, null );

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
        Assert.assertEquals( line, "exclusive2", "Jar contained: " + this.getFirstLineOfFile( artifact )
            + ", expected: exclusive2" );

        artifact = this.downloadArtifactFromGroup( "other-group", gav, "target/downloads/exclude" );
        Assert.assertNotNull( artifact );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( line, "exclusive1", "Jar contained: " + line + ", expected: exclusive1" );

    }

    @Test
    public void testInclusive()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".inclusive", "inclusive", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, null, false, null );

        File artifact = this.downloadArtifactFromGroup( "inclusive-single", gav, "target/downloads/include" );

        String line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( line, "inclusive1", "Jar contained: " + this.getFirstLineOfFile( artifact )
            + ", expected: inclusive1" );

        artifact = this.downloadArtifactFromGroup( "inclusive-group", gav, "target/downloads/include" );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( line, "inclusive2", "Jar contained: " + this.getFirstLineOfFile( artifact )
            + ", expected: inclusive2" );

        artifact = this.downloadArtifactFromGroup( "other-group", gav, "target/downloads/include" );

        line = this.getFirstLineOfFile( artifact );
        Assert.assertEquals( line, "inclusive1", "Jar contained: " + this.getFirstLineOfFile( artifact )
            + ", expected: inclusive1" );

    }

    @Test
    public void testBlocking()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId() + ".blocking", "blocking", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, null, false, null );

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
        Assert.assertEquals( line, "blocking1", "Jar contained: " + this.getFirstLineOfFile( artifact )
            + ", expected: blocking1" );

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
