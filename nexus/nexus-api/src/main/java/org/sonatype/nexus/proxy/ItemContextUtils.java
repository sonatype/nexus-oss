package org.sonatype.nexus.proxy;

import java.util.Map;

/**
 * A collection of static util methods to handle the item context, a context that is live during request processing.
 * 
 * @author cstamas
 */
public class ItemContextUtils
{
    private static final String CONDITION_IF_MODIFIED_SINCE = "condition.ifModifiedSince";

    public static boolean isConditional( Map<String, Object> ctx )
    {
        return ctx.containsKey( CONDITION_IF_MODIFIED_SINCE );
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
}
