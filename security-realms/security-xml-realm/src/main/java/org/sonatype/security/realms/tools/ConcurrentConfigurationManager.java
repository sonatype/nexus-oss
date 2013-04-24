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
package org.sonatype.security.realms.tools;

/*
 * Extends the ConfigurationManager facade interface to add thread safety
 * 
 * @author Steve Carlucci
 * @since 3.0.3
 */
public interface ConcurrentConfigurationManager extends ConfigurationManager
{
    /*
     * Runs the provided action in a thread-safe way. Any write-based ConfigurationManager calls, or
     * operations requiring multiple ConfigurationManager calls, must be executed in an action via a call
     * to this method.
     * 
     * Any direct calls to write-based ConfigurationManager methods will throw an UnsupportedOperationException, as they
     * cannot be used directly in a thread-safe manner
     * 
     * Direct calls to read-based ConfigurationManager methods can be called in a thread-safe manner. However, operations
     * that require multiple read-based calls should be encapsulated into an action and executed via this method
     * 
     * The type parameters represent the exceptions that can be thrown by the ConfigurationManager calls in the provided
     * action. If one or both are not needed, specify RuntimeException.
     */
    <X1 extends Exception, X2 extends Exception> void run(ConfigurationManagerAction action) throws X1, X2;
}
