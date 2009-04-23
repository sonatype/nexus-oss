/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.configuration.source;

import java.io.IOException;

import org.sonatype.security.configuration.model.SecurityConfiguration;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch security configuration by some means. It also
 * stores one instance of Configuration object maintained thru life of the application. This component is also able to
 * persist security config.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationSource
{

    // /**
    // * Returns the validation response, if any. It is created on the loading of the user configuration.
    // *
    // * @return the response or null if not applicable or config was still not loaded.
    // */
    // ValidationResponse getValidationResponse();

    /**
     * Persists the current configuration.
     * 
     * @throws IOException
     */
    void storeConfiguration()
        throws IOException;

     /**
     * Gets the current configuration.
     *
     * @return the configuration, null if not loaded
     * @throws SecurityConfigurationException
     * @throws IOException
     */
     SecurityConfiguration getConfiguration();
        
    
     /**
     * Forces reloading the user configuration.
     *
     * @return the configuration
     * @throws SecurityConfigurationException
     * @throws IOException
     */
     SecurityConfiguration loadConfiguration()
     throws SecurityConfigurationException,
     IOException;

    /**
     * Returns the configuration that this configuration uses for defaulting.
     * 
     * @return a config source that is default source for this config or null
     */
    SecurityConfigurationSource getDefaultsSource();

    /**
     * Returns whether the configuration was upgraded.
     * 
     * @return true if the user configuration was upgraded, false otherwise
     */
    boolean isConfigurationUpgraded();

    /**
     * Returns true if the configuration was got from defaults.
     * 
     * @return
     */
    boolean isConfigurationDefaulted();
}
