package org.sonatype.appcontext.publisher;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.ContextStringDumper;

public abstract class AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
    public String getDumpAsString( final AppContext context )
    {
        return getDumpAsString( context, false );
    }

    public String getDumpAsString( final AppContext context, final boolean recursively )
    {
        final StringBuilder sb = new StringBuilder();
        if ( recursively && context.getParent() != null )
        {
            sb.append( getDumpAsString( context.getParent(), recursively ) );
        }
        sb.append( ContextStringDumper.dumpToString( context ) );
        return sb.toString();
    }
}
