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
package org.sonatype.security.web;

/**
 * This component will manage how paths are dynamically added to the security infrastructure.
 * 
 * @author Brian Demers
 */
public interface ProtectedPathManager
{
    /**
     * Adds a protected resource for the <codepathPattern</code>, and configures it with the
     * <code>filterExpression</code>.
     * 
     * @param pathPattern the pattern of the path to protect (i.e. ant pattern)
     * @param filterExpression the configuration used for the filter protecting this pattern.
     */
    public void addProtectedResource( String pathPattern, String filterExpression );

    // TODO: this may require some shiro changes if we need this in the future.
    // /**
    // * Removes a protected path
    // * @param pathPattern path to be removed
    // */
    // public void removeProtectedResource( String pathPattern );
}
