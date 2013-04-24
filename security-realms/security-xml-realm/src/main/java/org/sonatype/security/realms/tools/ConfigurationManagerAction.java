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

/**
 * Defines an interface for a ConfigurationManager action. These actions
 * are intended to encapsulate higher-level operations that require multiple calls
 * to the ConfigurationManager to complete. These actions are used by the ConcurrentConfigurationManager
 * to provide a way for users to use the ConfigurationManager in a thread-safe manner
 * 
 * @author Steve Carlucci
 * @since 3.0.3
 */
public interface ConfigurationManagerAction
{
    /**
     * Access the type of this action (e.g. read/write)
     * 
     * @return the action type
     */
    ConfigurationManagerActionType getActionType();
    
    /**
     * Run the action
     * 
     * The type parameters allow for different exception types based on
     * the ConfigurationManager methods that are called in the action
     */
    <X1 extends Exception, X2 extends Exception> void run() throws X1, X2;
}
