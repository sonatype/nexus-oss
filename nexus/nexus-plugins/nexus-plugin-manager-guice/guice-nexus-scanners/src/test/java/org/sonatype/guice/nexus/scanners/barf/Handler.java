/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.nexus.scanners.barf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler
    extends URLStreamHandler
{
    @Override
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        return new DodgyConnection( url );
    }

    static class DodgyConnection
        extends URLConnection
    {
        DodgyConnection( final URL url )
        {
            super( url );
        }

        @Override
        public void connect()
            throws IOException
        {
        }

        @Override
        public InputStream getInputStream()
            throws IOException
        {
            return new InputStream()
            {
                @Override
                public int read()
                    throws IOException
                {
                    throw new IOException();
                }

                @Override
                public void close()
                    throws IOException
                {
                    throw new IOException();
                }
            };
        }
    }
}
