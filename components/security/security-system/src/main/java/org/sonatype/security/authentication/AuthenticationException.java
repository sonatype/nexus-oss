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
package org.sonatype.security.authentication;

/**
 * Thrown when a Subject or Principal could not be authenticated.
 * 
 * @author Brian Demers
 */
public class AuthenticationException
    extends Exception
{

    private static final long serialVersionUID = 5307046352518675119L;

    public AuthenticationException()
    {
    }

    public AuthenticationException( String message )
    {
        super( message );
    }

    public AuthenticationException( Throwable cause )
    {
        super( cause );
    }

    public AuthenticationException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
