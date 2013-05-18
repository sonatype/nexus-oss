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
package org.sonatype.appcontext.source.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * EntryFilter that filters on key-equality (keys are Strings) using key.equals() method, hence, this is case sensitive!
 * You can supply a list of keys to accept.
 * 
 * @author cstamas
 */
public class KeyEqualityEntryFilter
    implements EntryFilter
{
    /**
     * The list of keys to accept.
     */
    private final List<String> keys;

    public KeyEqualityEntryFilter( final String... keys )
    {
        this( Arrays.asList( keys ) );
    }

    public KeyEqualityEntryFilter( final List<String> keys )
    {
        this.keys = Collections.unmodifiableList( Preconditions.checkNotNull( keys ) );
    }

    public boolean accept( final String key, final Object value )
    {
        return keys.contains( key );
    }

    public EntrySourceMarker getFilteredEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "filter(keyIsIn:%s, %s)", keys, wrapped.getDescription() );
            }
        };
    }
}
