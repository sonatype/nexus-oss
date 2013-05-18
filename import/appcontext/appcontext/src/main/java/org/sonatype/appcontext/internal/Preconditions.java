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
package org.sonatype.appcontext.internal;

/**
 * Guava, we love you :D But I'd like to keep dependencies to minimum.
 * 
 * @author cstamas
 */
public class Preconditions
{
    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     * 
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull( T reference )
    {
        return checkNotNull( reference, "Argument cannot be null!" );
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     * 
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails; will be converted to a string using
     *            {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull( T reference, Object errorMessage )
    {
        if ( reference == null )
        {
            throw new NullPointerException( String.valueOf( errorMessage ) );
        }
        return reference;
    }
}
