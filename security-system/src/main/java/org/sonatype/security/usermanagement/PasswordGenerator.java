/*
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
package org.sonatype.security.usermanagement;

/**
 * Generates passwords for users.
 * 
 * @author Brian Demers
 */
public interface PasswordGenerator
{
    /**
     * Generates a password.
     * 
     * @param minChars the minimum number of characters in the password.
     * @param maxChars the maximum number of characters in the password.
     * @return
     */
    String generatePassword( int minChars, int maxChars );
    
    /**
     * Generate a random string of data to be used to salt a password hash
     * 
     * @return the generated salt
     */
    String generateSalt();

    /**
     * Hash a password String.
     * 
     * @param password to be hashed.
     * @return the hash password String.
     * @deprecated use only to generate legacy unsalted password hashes
     */
    String hashPassword( String password );
    
    /**
     * Hash a password, using the specified salt, for the specified
     * number of iterations
     * 
     * @param clearPassword password to be hashed
     * @param salt used to randomize and strengthen the hash
     * @param hashIterations number of hash iterations to perform. Provides key stretching
     * @return the hashed password
     */
    String hashPassword( String clearPassword, String salt, int hashIterations);
}
