package org.sonatype.appcontext.publisher;

import java.io.PrintStream;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A EntryPublisher the publishes the contexts to supplied {@code java.io.PrintStream}, or to {@code System.out}.
 * 
 * @author cstamas
 */
public class PrintStreamEntryPublisher
    extends AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
    /**
     * The PrintStream to be used for publishing.
     */
    private final PrintStream printStream;

    /**
     * Constructs publisher the publishes to {@code System.out}.
     */
    public PrintStreamEntryPublisher()
    {
        this( System.out );
    }

    /**
     * Constructs publisher to use supplied print stream.
     * 
     * @param printStream
     * @throws NullPointerException if {@code preintStream} is null
     */
    public PrintStreamEntryPublisher( final PrintStream printStream )
    {
        this.printStream = Preconditions.checkNotNull( printStream );
    }

    public void publishEntries( final AppContext context )
    {
        printStream.println( getDumpAsString( context ) );
    }
}
