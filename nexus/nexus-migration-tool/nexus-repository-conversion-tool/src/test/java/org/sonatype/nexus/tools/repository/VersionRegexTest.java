/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.tools.repository;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Juven Xu
 *
 */
public class VersionRegexTest
{
    @Test
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
            Assert.assertTrue( version.matches( DefaultRepositoryConvertor.VERSION_REGEX ) );
        }

        for ( String version : unmatched )
        {
            Assert.assertFalse( version.matches( DefaultRepositoryConvertor.VERSION_REGEX ) );
        }
    }
}
