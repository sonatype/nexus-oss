package org.sonatype.appcontext.internal;

import java.util.Map.Entry;

import org.sonatype.appcontext.AppContext;

public class ContextStringDumper
{
    public static final String dumpToString( final AppContext context )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Application context \"" + context.getId() + "\" dump:\n" );

        for ( Entry<String, Object> entry : context.entrySet() )
        {
            sb.append( String.format( "\"%s\"=\"%s\"\n", String.valueOf( entry.getKey() ),
                String.valueOf( entry.getValue() ) ) );
        }

        sb.append( String.format( "Total of %s entries.\n", context.size() ) );

        return sb.toString();
    }
}
