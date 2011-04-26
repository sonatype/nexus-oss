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
package org.sonatype.nexus.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author juven
 */
public class ItemPathUtilsTest
{
    @Test
    public void testGetLCPPathFromPair()
        throws Exception
    {
        String pathA = "";
        String pathB = "/org";
       Assert. assertNull( ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = null;
        pathB = "/org";
       Assert. assertNull( ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/org/apache/maven";
        pathB = "/org/apache/";
        String expected = "/org/apache/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "org/sonatype/nexus/";
        pathB = "org/sonatype/nexus/nexus-api";
        expected = "org/sonatype/nexus/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/commons-attributes/commons-attributes-api/2.1/commons-attributes-api-2.1.pom";
        pathB = "/commons-io/commons-io/1.3.1/commons-io-1.3.1.pom";
        expected = "/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/commons-io/commons-io/1.4/commons-io-1.4-sources.jar";
        pathB = "/commons-io/commons-io/1.3.1/commons-io-1.3.1.pom";
        expected = "/commons-io/commons-io/";
       Assert. assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );

        pathA = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom";
        pathB = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom";
        expected = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( pathA, pathB ) );
    }

    @Test
    public void testGetLCPPathFromCollection()
        throws Exception
    {
        List<String> paths = new ArrayList<String>();
        Assert.assertNull( ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "" );
        paths.add( "/org/apache" );
        Assert.assertNull( ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/" );
        String expected = "/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom" );
        expected = "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090212.053150-427.pom" );
        paths.add( "/org/sonatype/nexus/nexus/1.2.0.4/nexus-1.2.0.4.pom" );
        expected = "/org/sonatype/nexus/nexus/";
       Assert. assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/org/apache/maven/plugins/maven-archetype-plugin/2.0-alpha-5-SNAPSHOT" );
        paths.add( "/org/apache/maven/plugins/maven-idea-plugin/2.3-SNAPSHOT/maven-idea-plugin-2.3-SNAPSHOT.pom" );
        paths.add( "/org/apache/maven/plugins/maven-rar-plugin/2.2/maven-rar-plugin-2.2.pom" );
        expected = "/org/apache/maven/plugins/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );

        paths.clear();
        paths.add( "/" );
        paths.add( "/org/apache/maven/plugins/maven-archetype-plugin/2.0-alpha-5-SNAPSHOT" );
        expected = "/";
        Assert.assertEquals( expected, ItemPathUtils.getLCPPath( paths ) );
    }
}
