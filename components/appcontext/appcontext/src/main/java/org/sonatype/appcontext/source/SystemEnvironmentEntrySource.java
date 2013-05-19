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

/**
 * EntrySource that sources itself from {@code System.getenv()} call. It also may perform "normalization" of the keys,
 * as:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>replaces all occurrences of character '_' (underscore) to '.' (dot)</li>
 * </ul>
 * This is needed to make it possible to have different sources have same keys.
 * 
 * @author cstamas
 */
public class SystemEnvironmentEntrySource
    implements EntrySource, EntrySourceMarker
{
    public String getDescription()
    {
        return "system(env)";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, String> envMap = System.getenv();
        final Map<String, Object> result = new HashMap<String, Object>();
        for ( Map.Entry<String, String> entry : envMap.entrySet() )
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
