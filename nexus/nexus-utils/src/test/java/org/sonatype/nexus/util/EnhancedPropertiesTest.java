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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;

import junit.framework.TestCase;

/**
 * @author juven
 */
public class EnhancedPropertiesTest
    extends TestCase
{

    private static String LINE_SEPARATOR = System.getProperty( "line.separator" );

    public void testRead()
        throws Exception
    {
        String source = "author=juven" + LINE_SEPARATOR + LINE_SEPARATOR + "#Juven Xu" + LINE_SEPARATOR + "date=Mar 11"
            + LINE_SEPARATOR;

        EnhancedProperties properties = new EnhancedProperties();
        InputStream inStream = new ByteArrayInputStream( source.getBytes() );
        properties.load( inStream );
        inStream.close();

        LinkedHashSet<String> expected = new LinkedHashSet<String>();
        expected.add( "juven" );
        expected.add( "" );
        expected.add( "#Juven Xu" );
        expected.add( "Mar 11" );

        LinkedHashSet<String> actual = new LinkedHashSet<String>();
        for ( String value : properties.values() )
        {
            actual.add( value );
        }

        assertEquals( expected, actual );
    }

    public void testStore()
        throws Exception
    {
        String source = "author=juven" + LINE_SEPARATOR + LINE_SEPARATOR + "#Juven Xu" + LINE_SEPARATOR + "date=Mar 11"
            + LINE_SEPARATOR;

        EnhancedProperties properties = new EnhancedProperties();
        InputStream inStream = new ByteArrayInputStream( source.getBytes() );
        properties.load( inStream );
        inStream.close();

        properties.put( "author", "juv-away" );
        properties.put( "date", "Mar 11, 2009" );

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        properties.store( outStream, "a comment" );

        String expected = "# a comment" + LINE_SEPARATOR + LINE_SEPARATOR + "author=juv-away" + LINE_SEPARATOR + LINE_SEPARATOR + "#Juven Xu" + LINE_SEPARATOR
            + "date=Mar 11, 2009" + LINE_SEPARATOR;
        assertEquals( expected, outStream.toString() );

        outStream.close();

    }
}
