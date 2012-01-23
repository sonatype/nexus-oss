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
package org.sonatype.nexus.plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SettingsMap
    extends LinkedHashMap<String, String>
    implements Map<String, String>
{

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public SettingsMap( Properties p )
    {
        super( (Map) p );
    }

    private static final long serialVersionUID = 3965158074707752467L;

    @Override
    public String get( Object k )
    {
        String[] keys = ( (String) k ).split( "\\|" );
        String key;
        String defaultValue = null;

        switch ( keys.length )
        {
            default:
                throw new IllegalArgumentException( "Invalid key " + k );
            case 3:
                // description, so?
            case 2:
                defaultValue = keys[1];
            case 1:
                key = keys[0];
        }

        String value = super.get( key );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
