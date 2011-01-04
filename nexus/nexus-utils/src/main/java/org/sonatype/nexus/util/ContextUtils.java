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

import java.util.Collection;
import java.util.Map;

public class ContextUtils
{
    public static boolean isFlagTrue( Map<String, Object> context, String key )
    {
        if ( context != null )
        {
            if ( context.containsKey( key ) )
            {
                return Boolean.TRUE.equals( context.get( key ) );
            }
        }

        return false;
    }

    public static void setFlag( Map<String, Object> context, String key, boolean value )
    {
        if ( context != null )
        {
            if ( value )
            {
                context.put( key, Boolean.TRUE );
            }
            else
            {
                context.remove( key );
            }
        }
    }

    public static boolean collContains( Map<String, Object> context, String key, Object value )
    {
        if ( context != null && context.containsKey( key ) )
        {
            if ( context.get( key ) instanceof Collection )
            {
                Collection<?> coll = (Collection<?>) context.get( key );

                return coll.contains( value );
            }
        }

        return false;
    }

    @SuppressWarnings( "unchecked" )
    public static boolean collAdd( Map<String, Object> context, String key, Object value )
    {
        if ( context != null && context.containsKey( key ) )
        {
            if ( context.get( key ) instanceof Collection )
            {
                Collection<Object> coll = (Collection<Object>) context.get( key );

                return coll.add( value );
            }
        }

        return false;
    }

}
