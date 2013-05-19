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

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * Entry source that sources from supplied AppContext. This is not the same as setting AppContext parent!
 * 
 * @author cstamas
 */
public class AppContextEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final AppContext context;

    /**
     * Constructs the AppContextEntrySource with supplied AppContext.
     * 
     * @param context
     * @throws NullPointerException when supplied context is null,
     */
    public AppContextEntrySource( final AppContext context )
    {
        this.context = Preconditions.checkNotNull( context );
    }

    public String getDescription()
    {
        return "appcontext(" + context.getId() + ")";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>( context.size() );

        for ( Map.Entry<String, Object> entry : context.entrySet() )
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return result;
    }
}
