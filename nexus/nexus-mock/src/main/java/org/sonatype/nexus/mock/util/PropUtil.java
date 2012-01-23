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
package org.sonatype.nexus.mock.util;

public class PropUtil
{
    public static String get( String name, String def )
    {
        String val = System.getProperty( name, def );
        if ( val == null || "".endsWith( val ) || ( val.startsWith( "${" ) && val.endsWith( "}" ) ) )
        {
            val = def;
        }

        return val;
    }

    public static int get( String name, int def )
    {
        return Integer.parseInt( get( name, String.valueOf( def ) ) );
    }
}
