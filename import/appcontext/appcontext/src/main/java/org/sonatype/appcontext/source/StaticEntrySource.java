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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A static EntrySource that holds the key and value to make it into AppContext. Useful in testing, or when you need to
 * add one key=value into context, and you need to calculate those somehow before constructing AppContext.
 * 
 * @author cstamas
 */
public class StaticEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String key;

    private final Object value;

    public StaticEntrySource( final String key, final Object val )
    {
        this.key = Preconditions.checkNotNull( key );
        this.value = val;
    }

    public String getDescription()
    {
        if ( value != null )
        {
            return String.format( "static(\"%s\"=\"%s\")", key, String.valueOf( value ) );
        }
        else
        {
            return String.format( "static(\"%s\"=null)", key );
        }
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>( 1 );
        result.put( key, value );
        return result;
    }
}
