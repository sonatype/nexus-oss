/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
