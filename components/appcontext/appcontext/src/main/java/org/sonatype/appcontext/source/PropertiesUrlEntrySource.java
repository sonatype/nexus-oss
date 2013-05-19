/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appcontext.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * EntrySource that sources itself from a {@code java.util.Properties} loaded from a {@link URL}. It might be set to
 * fail but also to keep silent the fact that URL to load is not found (not exists).
 * 
 * @author cstamas
 */
public class PropertiesUrlEntrySource
    extends AbstractMapEntrySource
{
    private final URL propertiesUrl;

    private final boolean failIfNotFound;

    private Map<String, Object> source;

    public PropertiesUrlEntrySource( final URL propertiesUrl )
    {
        this( propertiesUrl, true );
    }

    public PropertiesUrlEntrySource( final URL propertiesUrl, final boolean failIfNotFound )
    {
        super( Preconditions.checkNotNull( propertiesUrl ).toString(), "propsUrl" );
        this.propertiesUrl = Preconditions.checkNotNull( propertiesUrl );
        this.failIfNotFound = failIfNotFound;
    }

    public synchronized Map<String, Object> getSource()
        throws AppContextException
    {
        if ( source == null )
        {
            try
            {
                final Properties properties = new Properties();
                InputStream is = propertiesUrl.openStream();

                try
                {
                    if ( propertiesUrl.getPath().endsWith( ".xml" ) )
                    {
                        // assume it's new XML properties file
                        properties.loadFromXML( is );
                    }
                    else
                    {
                        // assume it's "plain old" properties file
                        properties.load( is );
                    }
                }
                finally
                {
                    is.close();
                }

                final Map<String, Object> result = new HashMap<String, Object>();

                for ( Map.Entry<Object, Object> entry : properties.entrySet() )
                {
                    final String key = String.valueOf( entry.getKey() );

                    result.put( key, entry.getValue() );
                }

                source = result;
            }
            catch ( FileNotFoundException e )
            {
                if ( failIfNotFound )
                {
                    throw new AppContextException( "Cannot load up properties file from \"" + propertiesUrl
                        + "\", it does not exists!" );
                }
                else
                {
                    source = Collections.emptyMap();
                }
            }
            catch ( IOException e )
            {
                throw new AppContextException( "Cannot load up properties file from \"" + propertiesUrl + "\"!", e );
            }
        }

        return source;
    }
}
