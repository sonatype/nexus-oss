/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.artifactorybridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class URLInputStreamRepresentation
    extends OutputRepresentation
{

    private InputStream input;

    private HttpURLConnection urlConn;

    public URLInputStreamRepresentation( String type, InputStream input, HttpURLConnection urlConn )
    {
        super( MediaType.valueOf( type ) );
        if ( input == null )
        {
            throw new NullPointerException( "input" );
        }
        if ( urlConn == null )
        {
            throw new NullPointerException( "urlConn" );
        }
        this.input = input;
        this.urlConn = urlConn;
    }

    @Override
    public void write( OutputStream out )
        throws IOException
    {
        IOUtil.copy( input, out );
        out.flush();
    }

    @Override
    public void release()
    {
        IOUtil.close( input );
        urlConn.disconnect();

        input = null;
        urlConn = null;

        super.release();
    }
}
