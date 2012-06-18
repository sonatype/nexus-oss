/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.events;

import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * An event fired when a user is removed from the system.
 * 
 * @since 2.8
 */
public class UserPrincipalsExpiredEvent
    extends AbstractEvent<Object>
{
    private final String userId;

    private final String source;

    /**
     * @param component The sending component
     * @param userId The removed user's id
     * @param source The UserManager source
     */
    public UserPrincipalsExpiredEvent( Object component, String userId, String source )
    {
        super( component );

        this.userId = userId;
        this.source = source;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getSource()
    {
        return source;
    }
}
