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
package org.sonatype.appcontext;

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * Represents an entry from {@link AppContext}. You usually do not want to tamper with these, as {@link AppContext}
 * exposes entry values directly over it's map-like interface.
 * 
 * @author cstamas
 */
public interface AppContextEntry
{
    /**
     * Returns the creation timestamp in millis of this entry.
     * 
     * @return millisecond timestamp when this entry was created.
     */
    long getCreated();

    /**
     * Returns the key this entry is keyed with.
     * 
     * @return the key of entry.
     */
    String getKey();

    /**
     * Returns the value this entry holds. In case of string type, it will be interpolated.
     * 
     * @return the value of entry, interpolated if value type is string.
     */
    Object getValue();

    /**
     * Returns the "raw" value of the entry. It might differ from {@link #getValue()} in case of string types values, as
     * this will return uninterpolated value.
     * 
     * @return the raw value of entry, uninterpolated if value is string.
     */
    Object getRawValue();

    /**
     * Returns the marker denoting from where this entry came from.
     * 
     * @return the marker denoting the origin of this entry.
     */
    EntrySourceMarker getEntrySourceMarker();
}
