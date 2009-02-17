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
package org.sonatype.nexus.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author juven
 */
public class ItemPathUtilsTest
    extends TestCase
{
    public void testGetLCPPathFromPair()
        throws Exception
    {
        String pathA = "";
        String pathB = "/org";
        assertNull( ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = null;
        pathB = "/org";
        assertNull( ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/org/apache/maven";
        pathB = "/org/apache/";
        String expected = "/org/apache/";
        assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "org/sonatype/nexus/";
        pathB = "org/sonatype/nexus/nexus-api";
        expected = "org/sonatype/nexus/";
        assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/commons-attributes/commons-attributes-api/2.1/commons-attributes-api-2.1.pom";
        pathB = "/commons-io/commons-io/1.3.1/commons-io-1.3.1.pom";
        expected = "/";
        assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/commons-io/commons-io/1.4/commons-io-1.4-sources.jar";
        pathB = "/commons-io/commons-io/1.3.1/commons-io-1.3.1.pom";
        expected = "/commons-io/commons-io/";
        assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom";
        pathB = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom";
        expected = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/";
        assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );
    }

    public void testGetLCPPathFromCollection()
        throws Exception
    {
        List<String> paths = new ArrayList<String>();
        assertNull( ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "" );
        paths.add( "/org/apache" );
        assertNull( ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/" );
        String expected = "/";
        assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom" );
        expected = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/";
        assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.2.0.4/nexus-1.2.0.4.pom" );
        expected = "/org/sonatype/nexus/nexus/";
        assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/apache/maven/plugins/maven-archetype-plugin/2.0-alpha-5-SNAPSHOT" );
        paths.add( "/org/apache/maven/plugins/maven-idea-plugin/2.3-SNAPSHOT/maven-idea-plugin-2.3-SNAPSHOT.pom" );
        paths.add( "/org/apache/maven/plugins/maven-rar-plugin/2.2/maven-rar-plugin-2.2.pom" );
        expected = "/org/apache/maven/plugins/";
        assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/" );
        paths.add( "/org/apache/maven/plugins/maven-archetype-plugin/2.0-alpha-5-SNAPSHOT" );
        expected = "/";
        assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );
    }
}
