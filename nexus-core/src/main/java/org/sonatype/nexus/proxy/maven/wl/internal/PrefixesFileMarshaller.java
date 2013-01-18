package org.sonatype.nexus.proxy.maven.wl.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.maven.wl.EntrySource;

import com.google.common.io.Closeables;

/**
 * Simple text based file with prefixes with dead simple syntax: Lines starting with '#' are comments, and any other
 * line is actually a prefix.
 * 
 * @author cstamas
 * @since 2.4
 */
public class PrefixesFileMarshaller
    implements EntrySourceMarshaller
{
    protected static final Charset CHARSET = Charset.forName( "UTF-8" );

    @Override
    public void write( final EntrySource entrySource, final OutputStream outputStream )
        throws IOException
    {
        final List<String> entries = entrySource.readEntries();
        final PrintWriter printWriter = new PrintWriter( new OutputStreamWriter( outputStream, CHARSET ) );
        for ( String entry : entries )
        {
            printWriter.println( entry );
        }
        printWriter.flush();
    }

    @Override
    public EntrySource read( final InputStream inputStream )
        throws IOException
    {
        try
        {
            final ArrayList<String> entries = new ArrayList<String>();
            final BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, CHARSET ) );
            String line = reader.readLine();
            while ( line != null )
            {
                // trim
                line = line.trim();
                if ( !line.startsWith( "#" ) )
                {
                    // Igor's find command makes path like "./org/apache/"
                    while ( line.startsWith( "." ) )
                    {
                        line = line.substring( 1 );
                    }
                    // win file separators? Highly unlikely but still...
                    line = line.replace( '\\', '/' );

                    entries.add( line );
                }
                line = reader.readLine();
            }
            return new ArrayListEntrySource( entries );
        }
        finally
        {
            Closeables.closeQuietly( inputStream );
        }
    }
}
