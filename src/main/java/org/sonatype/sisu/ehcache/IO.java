package org.sonatype.sisu.ehcache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Code borrowed from Plexus Utils to keep dependencies minimal. Only sole purpose of this code is to read up EHCache
 * configuration from classpath as string.
 * 
 * @author cstamas
 * @since 1.0
 */
public class IO
{
    public static String toString( final InputStream input )
        throws IOException
    {
        final StringWriter sw = new StringWriter();
        copy( new InputStreamReader( input, "UTF-8" ), sw, 4096 );
        return sw.toString();
    }

    public static void copy( final Reader input, final Writer output, final int bufferSize )
        throws IOException
    {
        final char[] buffer = new char[bufferSize];
        int n = 0;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
        output.flush();
    }
}
