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
package org.sonatype.security.realms.tools;

/**
 * Defines an interface for a ConfigurationManager action. These actions
 * are intended to encapsulate higher-level operations that require multiple calls
 * to the ConfigurationManager to complete. These actions provide a way for users to use the ConfigurationManager
 * in a thread-safe manner. Must be used in conjunction with an implementation of ConfigurationManager that supports
 * the runRead and runWrite methods
 * 
 * @author Steve Carlucci
 * @since 3.1
 */
public interface ConfigurationManagerAction
{
    /**
     * Run the action
     */
    void run() throws Exception;
}
