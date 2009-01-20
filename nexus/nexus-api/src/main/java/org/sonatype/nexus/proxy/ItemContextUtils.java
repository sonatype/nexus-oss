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
package org.sonatype.nexus.proxy;

import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

/**
 * A collection of static util methods to handle the item context, a context that is live during request processing.
 * 
 * @author cstamas
 */
public class ItemContextUtils
{
    private static final String CONDITION_IF_MODIFIED_SINCE = "condition.ifModifiedSince";

    private static final String CONDITION_IF_NONE_MATCH = "condition.ifNoneMatch";

    public static boolean isConditional( Map<String, Object> ctx )
    {
        return ctx.containsKey( CONDITION_IF_MODIFIED_SINCE ) || ctx.containsKey( CONDITION_IF_NONE_MATCH );
    }

    public static long getIfModifiedSince( Map<String, Object> ctx )
    {
        return ( (Long) ctx.get( CONDITION_IF_MODIFIED_SINCE ) ).longValue();
    }

    public static void setIfModifiedSince( Map<String, Object> ctx, long ifModifiedSince )
    {
        if ( ifModifiedSince != 0 )
        {
            ctx.put( CONDITION_IF_MODIFIED_SINCE, Long.valueOf( ifModifiedSince ) );
        }
        else
        {
            ctx.remove( CONDITION_IF_MODIFIED_SINCE );
        }
    }

    public static String getIfNoneMatch( Map<String, Object> ctx )
    {
        return (String) ctx.get( CONDITION_IF_NONE_MATCH );
    }

    public static void setIfNoneMatch( Map<String, Object> ctx, String tag )
    {
        if ( !StringUtils.isEmpty( tag ) )
        {
            ctx.put( CONDITION_IF_NONE_MATCH, tag );
        }
        else
        {
            ctx.remove( CONDITION_IF_NONE_MATCH );
        }
    }
}
