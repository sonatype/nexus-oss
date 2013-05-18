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

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * A filter for entries.
 * 
 * @author cstamas
 */
public interface EntryFilter
{
    /**
     * Returns the filtered entry source marker.
     * 
     * @param source
     * @return
     */
    EntrySourceMarker getFilteredEntrySourceMarker( EntrySourceMarker source );

    /**
     * Returns true if the key and entry is acceptable by this filter, otherwise false.
     * 
     * @param key
     * @param entry
     * @return true to accept or false to filter out the passed in key-value.
     */
    boolean accept( String key, Object entry );
}
