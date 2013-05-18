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

/**
 * Thrown when some fatal exception happens, that is probably not recoverable, but might be caused by wrong request. So,
 * caller might try again (ie. on user interaction or not).
 * 
 * @author cstamas
 */
public class AppContextException
    extends RuntimeException
{
    private static final long serialVersionUID = 3396476391595403414L;

    /**
     * @param message
     */
    public AppContextException( String message )
    {
        super( message );
    }

    /**
     * @param message
     * @param cause
     */
    public AppContextException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
