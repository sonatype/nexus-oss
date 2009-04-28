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
package org.sonatype.nexus.test.utils;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TestProperties
{

    private static ResourceBundle bundle;

    // i hate doing this, but its really easy, and this is for tests. this class can be replaced easy, if we need to...
    static
    {
        bundle = ResourceBundle.getBundle( "baseTest" );
    }

    public static String getString( String key )
    {
        return bundle.getString( key );
    }

    public static Integer getInteger( String key )
    {
        String value = bundle.getString( key );
        return new Integer( value );
    }

    public static Map<String, String> getAll()
    {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        Enumeration<String> keys = bundle.getKeys();
        while ( keys.hasMoreElements() )
        {
            String key = keys.nextElement();
            properties.put( key, bundle.getString( key ) );
        }
        return properties;
    }

}
