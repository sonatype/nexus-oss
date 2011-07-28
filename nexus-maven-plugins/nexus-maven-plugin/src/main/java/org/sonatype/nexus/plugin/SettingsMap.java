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
