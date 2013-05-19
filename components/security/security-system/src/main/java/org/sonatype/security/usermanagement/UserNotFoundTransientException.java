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
package org.sonatype.security.usermanagement;

/**
 * Thrown when a user could not be found due to a temporary condition, for example when an LDAP server is unavailable.
 * Repeating the operation may succeed in the future without any intervention by the application.
 * 
 * @since 2.8
 */
public class UserNotFoundTransientException
    extends UserNotFoundException
{
    private static final long serialVersionUID = 7565547428483146620L;

    public UserNotFoundTransientException( String userId, String message, Throwable cause )
    {
        super( userId, message, cause );
    }

    public UserNotFoundTransientException( String userId, String message )
    {
        super( userId, message );
    }

    public UserNotFoundTransientException( String userId )
    {
        super( userId );
    }
}
