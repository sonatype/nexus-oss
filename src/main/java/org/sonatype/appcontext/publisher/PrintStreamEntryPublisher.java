package org.sonatype.appcontext.publisher;

import java.io.PrintStream;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.ContextStringDumper;
import org.sonatype.appcontext.internal.Preconditions;

public class PrintStreamEntryPublisher
    implements EntryPublisher
{
    private final PrintStream printStream;

    public PrintStreamEntryPublisher()
    {
        this( System.out );
    }

    public PrintStreamEntryPublisher( final PrintStream printStream )
    {
        this.printStream = Preconditions.checkNotNull( printStream );
    }

    public void publishEntries( AppContext context )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "===================================\n" );
        sb.append( ContextStringDumper.dumpToString( context ) );
        sb.append( "===================================\n" );

        printStream.println( sb.toString() );
    }
}
