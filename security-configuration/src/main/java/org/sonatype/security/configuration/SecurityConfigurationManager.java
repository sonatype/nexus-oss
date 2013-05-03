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
package org.sonatype.security.configuration;

import java.util.List;

import org.sonatype.configuration.validation.InvalidConfigurationException;

public interface SecurityConfigurationManager
{

    void setEnabled( boolean enabled );

    boolean isEnabled();

    void setAnonymousAccessEnabled( boolean anonymousAccessEnabled );

    boolean isAnonymousAccessEnabled();

    void setAnonymousUsername( String anonymousUsername )
        throws InvalidConfigurationException;

    String getAnonymousUsername();

    void setAnonymousPassword( String anonymousPassword )
        throws InvalidConfigurationException;

    String getAnonymousPassword();
    
    /**
     * The number of iterations to be used when hashing passwords
     * 
     * @return number of hash iterations
     * @since 3.1
     */
    int getHashIterations();

    void setRealms( List<String> realms )
        throws InvalidConfigurationException;

    List<String> getRealms();

    /**
     * Clear the cache and reload from file
     */
    void clearCache();

    /**
     * Save to disk what is currently cached in memory
     */
    void save();

    /**
     * @return the id of a security manager to be used by default security system
     */
    String getSecurityManager();

    void setSecurityManager( String securityManager );

}
