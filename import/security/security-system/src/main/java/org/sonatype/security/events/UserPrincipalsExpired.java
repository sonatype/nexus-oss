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
package org.sonatype.security.events;

/**
 * An event fired when a user is removed from the system, so cached principals can be expired.
 *
 * @since 2.8
 */
public class UserPrincipalsExpired
{

    private final String userId;

    private final String source;

    /**
     * Applies to any cached user principals that have the given userId and UserManager source.
     *
     * @param userId The removed user's id
     * @param source The UserManager source
     */
    public UserPrincipalsExpired( final String userId, final String source )
    {
        this.userId = userId;
        this.source = source;
    }

    /**
     * Applies to all cached user principals that have an invalid userId or UserManager source.
     */
    public UserPrincipalsExpired()
    {
        this( null, null );
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
