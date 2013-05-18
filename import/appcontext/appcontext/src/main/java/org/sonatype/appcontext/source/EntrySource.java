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

import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

/**
 * A EntrySource provides key=values from various sources, that will make into the AppContext.
 * 
 * @author cstamas
 */
public interface EntrySource
{
    /**
     * Returns the entry source marker.
     * 
     * @return
     */
    EntrySourceMarker getEntrySourceMarker();

    /**
     * Returns a map of key=values to have them put into the AppContext.
     * 
     * @param request
     * @return a map of key=vqlues to be put into AppContext.
     * @throws AppContextException
     */
    Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException;
}
