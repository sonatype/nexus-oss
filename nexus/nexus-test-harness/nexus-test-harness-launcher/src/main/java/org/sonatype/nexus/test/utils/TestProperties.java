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
package org.sonatype.nexus.test.utils;

import java.io.File;
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

    public static File getFile( String key )
    {
        return new File( getString( key ) );
    }

}
