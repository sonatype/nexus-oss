package org.sonatype.appcontext.internal;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class ContextStringDumper
{
    public static final String dumpToString( final AppContext context )
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Application context \"" + context.getId() + "\" dump:\n" );
        if ( context.getParent() != null )
        {
            sb.append( "Parent context is \"" + context.getParent().getId() + "\"\n" );
        }
        for ( String key : context.keySet() )
        {
            final AppContextEntry entry = context.getAppContextEntry( key );
            sb.append( entry.toString() ).append( "\n" );
        }
        sb.append( String.format( "Total of %s entries.\n", context.size() ) );
        return sb.toString();
    }
}
