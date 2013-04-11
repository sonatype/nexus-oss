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
package org.sonatype.appcontext.source.keys;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * EntrySource that wraps another EntrySource and applies KeyTransformer to it.
 * 
 * @author cstamas
 */
public class KeyTransformingEntrySource
    implements EntrySource
{
    public static final KeyTransformer NOOP = new NoopKeyTransformer();

    private final EntrySource source;

    private final KeyTransformer keyTransformer;

    private final EntrySourceMarker sourceMarker;

    public KeyTransformingEntrySource( final EntrySource source, final KeyTransformer keyTransformer )
    {
        this.source = Preconditions.checkNotNull( source );

        this.keyTransformer = Preconditions.checkNotNull( keyTransformer );

        this.sourceMarker = keyTransformer.getTransformedEntrySourceMarker( source.getEntrySourceMarker() );
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
            result.put( keyTransformer.transform( entry.getKey() ), entry.getValue() );
        }

        return result;
    }

}
