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
package org.sonatype.nexus.proxy.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class MUtilsTest
{

    private String[][] validDigests =
        {
            { "MD5 (pom.xml) = 68da13206e9dcce2db9ec45a9f7acd52", "68da13206e9dcce2db9ec45a9f7acd52" },
            { "68da13206e9dcce2db9ec45a9f7acd52 pom.xml", "68da13206e9dcce2db9ec45a9f7acd52" },
            { "68da13206e9dcce2db9ec45a9f7acd52        pom.xml", "68da13206e9dcce2db9ec45a9f7acd52" },
            { "93f402a80b5c40b7f32f68771ee57c27", "93f402a80b5c40b7f32f68771ee57c27" },
            { "bbb603f9f7a32a10eb539c1067992dabab58d33a", "bbb603f9f7a32a10eb539c1067992dabab58d33a" },
            { "ant-1.5.jar: 90 2A 36 0E CA D9 8A 34  B5 98 63 C1 E6 5B CF 71", "902a360ecad98a34b59863c1e65bcf71" },
            { "ant-1.5.jar: DCAB 88FC 2A04 3C24 79A6  DE67 6A2F 8179 E9EA 2167",
                "dcab88fc2a043c2479a6de676a2f8179e9ea2167" },
            { "90 2A 36 0E CA D9 8A 34  B5 98 63 C1 E6 5B CF 71", "902a360ecad98a34b59863c1e65bcf71" },
            { "DCAB 88FC 2A04 3C24 79A6  DE67 6A2F 8179 E9EA 2167", "dcab88fc2a043c2479a6de676a2f8179e9ea2167" },
            { "90 2A 36 0E CA D9 8A 34  B5 98 63 C1 E6 5B CF 71     pom.xml", "902a360ecad98a34b59863c1e65bcf71" },
            { "DCAB 88FC 2A04 3C24 79A6  DE67 6A2F 8179 E9EA 2167     pom.xml",
                "dcab88fc2a043c2479a6de676a2f8179e9ea2167" },
        };

    private InputStream stream( String string )
        throws IOException
    {
        return new ByteArrayInputStream( string.getBytes( "UTF-8" ) );
    }

    @Test
    public void testAcceptedDigests()
        throws IOException
    {
        for ( int i = 0; i < validDigests.length; i++ )
        {
            String test = validDigests[i][0];
            String expected = validDigests[i][1];

            String digest = MUtils.readDigestFromStream( stream( test ) );

            assertThat( "MUtils did not accept " + test, digest, notNullValue() );
            assertThat( "MUtils did not accept " + test, digest, equalTo( expected ) );
        }
    }

}
