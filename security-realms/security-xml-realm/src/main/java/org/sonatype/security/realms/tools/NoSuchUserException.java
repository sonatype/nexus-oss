/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms.tools;

public class NoSuchUserException
    extends Exception
{
    public NoSuchUserException()
    {
        super( "User not found!" );
    }

    public NoSuchUserException( String userId )
    {
        super( "User with id='" + userId + "' not found!" );
    }
    
    public NoSuchUserException( String userId, String message )
    {
        super( "User with id='" + userId + "' not found!: "+ message );
    }
    
    public NoSuchUserException( String userId, String message, Throwable throwable )
    {
        super( "User with id='" + userId + "' not found!: "+ message, throwable );
    }
}
