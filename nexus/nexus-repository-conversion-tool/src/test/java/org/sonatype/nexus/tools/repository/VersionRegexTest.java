/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
