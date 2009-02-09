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
