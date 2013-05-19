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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * EntrySource that wraps another EntrySource and applies EntryFilter to it.
 * 
 * @author cstamas
 */
public class FilteredEntrySource
    implements EntrySource
{
    private final EntrySource source;

    private final EntryFilter filter;

    private final EntrySourceMarker sourceMarker;

    public FilteredEntrySource( final EntrySource source, final EntryFilter filter )
    {
        this.source = Preconditions.checkNotNull( source );
        this.filter = Preconditions.checkNotNull( filter );
        this.sourceMarker = filter.getFilteredEntrySourceMarker( source.getEntrySourceMarker() );
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return sourceMarker;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();
        for ( Map.Entry<String, Object> entry : source.getEntries( request ).entrySet() )
        {
            if ( filter.accept( entry.getKey(), entry.getValue() ) )
            {
                result.put( entry.getKey(), entry.getValue() );
            }
        }
        return result;
    }
}
