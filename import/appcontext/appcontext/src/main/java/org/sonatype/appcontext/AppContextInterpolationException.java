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
 * Thrown when some fatal exception happens during interpolation, like cycle detected in expressions.
 * 
 * @author cstamas
 * @since 3.0
 */
public class AppContextInterpolationException
    extends AppContextException
{
    private static final long serialVersionUID = 7958491320532121743L;

    /**
     * @param message
     */
    public AppContextInterpolationException( String message )
    {
        super( message );
    }

    /**
     * @param message
     * @param cause
     */
    public AppContextInterpolationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
