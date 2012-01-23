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
