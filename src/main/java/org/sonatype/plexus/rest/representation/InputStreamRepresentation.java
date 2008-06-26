/**
 * Copyright (C) 2008 Sonatype Inc. 
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sonatype.plexus.rest.representation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class InputStreamRepresentation
    extends OutputRepresentation
{
    private InputStream is;

    public InputStreamRepresentation( MediaType mediaType, InputStream is )
    {
        super( mediaType );

        setTransient( true );

        this.is = is;
    }

    @Override
    public InputStream getStream()
        throws IOException
    {
        return is;
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        try
        {
            IOUtil.copy( is, outputStream );
        }
        finally
        {
            is.close();
        }
    }

}
