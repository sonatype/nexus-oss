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
 * An EntrySource that is sourced from a {@code java.util.Map}.
 * 
 * @author cstamas
 */
public abstract class AbstractMapEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String name;

    private final String type;

    public AbstractMapEntrySource( final String name, final String type )
    {
        this.name = Preconditions.checkNotNull( name );
        this.type = Preconditions.checkNotNull( type );
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return String.format( "%s(%s)", getType(), getName() );
    }

    public final EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<?, ?> entry : getSource().entrySet() )
        {
            if ( entry.getValue() != null )
            {
                result.put( String.valueOf( entry.getKey() ), String.valueOf( entry.getValue() ) );
            }
            else
            {
                result.put( String.valueOf( entry.getKey() ), null );
            }
        }

        return result;
    }

    // ==

    protected abstract Map<?, ?> getSource();
}
