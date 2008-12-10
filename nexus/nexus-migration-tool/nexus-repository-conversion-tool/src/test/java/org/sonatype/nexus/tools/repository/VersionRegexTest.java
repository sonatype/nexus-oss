/**
 * Sonatype NexusTM [Open Source Version].
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
package org.sonatype.nexus.tools.repository;

import junit.framework.TestCase;

/**
 * 
 * @author Juven Xu
 *
 */
public class VersionRegexTest
    extends TestCase
{
    public void testVersionRegex()
        throws Exception
    {
        String[] matchedVersions = {
            "1.0",
            "1.1",
            "1.1-beta-1",
            "9.0.1",
            "2.1.1",
            "1.0-SNAPSHOT",
            "1.1-SNAPSHOT",
            "2.0.1-SNAPSHOT",
            "2.0-alhpa-1-SNAPSHOT" };

        String[] unmatched = { "testng", "org", "apache", "com", "junit" };

        for ( String version : matchedVersions )
        {
            assertTrue( version.matches( DefaultRepositoryConvertor.VERSION_REGEX ) );
        }

        for ( String version : unmatched )
        {
            assertFalse( version.matches( DefaultRepositoryConvertor.VERSION_REGEX ) );
        }
    }
}
