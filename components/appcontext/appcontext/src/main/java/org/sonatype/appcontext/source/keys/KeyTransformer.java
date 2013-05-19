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

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * Key tranformer that may apply some transformation to the passed in key.
 * 
 * @author cstamas
 */
public interface KeyTransformer
{
    /**
     * Returns the transformed entry source marker.
     * 
     * @param source
     * @return
     */
    EntrySourceMarker getTransformedEntrySourceMarker( EntrySourceMarker source );

    /**
     * Performs the transformation of the key and returns the transformed key.
     * 
     * @param key to transform
     * @return transformed key
     */
    String transform( String key );
}
