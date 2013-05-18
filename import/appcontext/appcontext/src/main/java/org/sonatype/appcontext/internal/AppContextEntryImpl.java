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
package org.sonatype.appcontext.internal;

import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.source.EntrySourceMarker;

public class AppContextEntryImpl
    implements AppContextEntry
{
    private final long created;
    
    private final String key;

    private final Object rawValue;

    private final Object value;

    private final EntrySourceMarker entrySourceMarker;

    public AppContextEntryImpl( final long created, final String key, final Object rawValue, final Object value,
                                final EntrySourceMarker entrySourceMarker )
    {
        this.created = created;
        this.key = Preconditions.checkNotNull( key );
        this.rawValue = rawValue;
        this.value = value;
        this.entrySourceMarker = Preconditions.checkNotNull( entrySourceMarker );
    }

    public long getCreated()
    {
        return created;
    }

    public String getKey()
    {
        return key;
    }

    public Object getRawValue()
    {
        return rawValue;
    }

    public Object getValue()
    {
        return value;
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return entrySourceMarker;
    }

    // ==

    public String toString()
    {
        return String.format( "\"%s\"=\"%s\" (raw: \"%s\", src: %s)", key, String.valueOf( value ),
            String.valueOf( rawValue ), entrySourceMarker.getDescription() );
    }
}
