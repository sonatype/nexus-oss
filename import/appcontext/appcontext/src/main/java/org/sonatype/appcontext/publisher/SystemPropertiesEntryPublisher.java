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
package org.sonatype.appcontext.publisher;

import java.util.Map.Entry;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A publisher that publishes Application Context back to to System properties, probably prefixed with keyPrefix, to
 * make it available for other system components like loggers, caches, etc.
 * 
 * @author cstamas
 */
public class SystemPropertiesEntryPublisher
    implements EntryPublisher
{
    /**
     * The prefix to be used to prefix keys ("prefix.XXX"), if set.
     */
    private final String keyPrefix;

    /**
     * Flag to force publishing. Otherwise, the system property will be set only if does not exists.
     */
    private final boolean override;

    /**
     * Constructs a publisher without prefix, will publish {@code key=values} with keys as is in context.
     * 
     * @param override
     */
    public SystemPropertiesEntryPublisher( final boolean override )
    {
        this.keyPrefix = null;
        this.override = override;
    }

    /**
     * Constructs a publisher with prefix, will publish context with {@code prefix.key=value}.
     * 
     * @param keyPrefix
     * @param override
     * @throws NullPointerException if {@code keyPrefix} is null
     */
    public SystemPropertiesEntryPublisher( final String keyPrefix, final boolean override )
    {
        this.keyPrefix = Preconditions.checkNotNull( keyPrefix );
        this.override = override;
    }

    public void publishEntries( final AppContext context )
    {
        for ( Entry<String, Object> entry : context.entrySet() )
        {
            String key = entry.getKey();
            String value = String.valueOf( entry.getValue() );

            // adjust the key name and put it back to System properties
            String sysPropKey = keyPrefix == null ? key : keyPrefix + key;

            if ( override || System.getProperty( sysPropKey ) == null )
            {
                System.setProperty( sysPropKey, (String) value );
            }
        }
    }
}
