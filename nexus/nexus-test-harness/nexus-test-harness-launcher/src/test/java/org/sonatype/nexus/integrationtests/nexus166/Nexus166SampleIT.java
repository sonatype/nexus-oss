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
package org.sonatype.nexus.integrationtests.nexus166;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.runner.ConsoleLoggingRunner;


/**
 * A sample test and a good starting point: <a href='https://docs.sonatype.com/display/NX/Nexus+Test-Harness'>Nexus Test-Harness</a>
 */
//@RunWith( ConsoleLoggingRunner.class )
public class Nexus166SampleIT extends AbstractNexusIntegrationTest
{

    @Test
    public void sampleTest() throws IOException
    {
        log.debug( "This is just an example test" );
        log.debug( "I will show you how to do a few simple things..." );

        File exampleFile = this.getTestFile( "example.txt" );

        BufferedReader reader = new BufferedReader(new FileReader(exampleFile));

        // we only have one line to read.
        String exampleText = reader.readLine();
        reader.close();

        // you get the point...
        log.debug( "exampleText: "+ exampleText );
    }

}
