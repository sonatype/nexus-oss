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
package org.sonatype.nexus.integrationtests;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusStateUtil;

@RunWith( Suite.class )
@SuiteClasses( { IntegrationTestSuiteClasses.class, IntegrationTestSuiteClassesSecurity.class } )
public class IntegrationTestSuite
{
    private static ForkedAppBooter app;

    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {
        // copy default nexus.xml
        File testConfigFile = AbstractNexusIntegrationTest.getResource( "default-config/nexus.xml" );
        File outputFile =
            new File( AbstractNexusIntegrationTest.nexusBaseDir + "/"
                + AbstractNexusIntegrationTest.RELATIVE_WORK_CONF_DIR, "nexus.xml" );
        FileTestingUtils.fileCopy( testConfigFile, outputFile );

        app = NexusStateUtil.doHardStart();

        NexusStateUtil.doSoftStop();
    }

    @AfterClass
    public static void afterSuite()
        throws Exception
    {
        NexusStateUtil.doHardStop( false );
    }

}
