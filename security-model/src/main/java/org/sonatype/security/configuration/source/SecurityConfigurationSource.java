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

import org.sonatype.security.model.Configuration;
import org.sonatype.security.configuration.ConfigurationException;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch security configuration by some means. It also stores one
 * instance of Configuration object maintained thru life of the application. This component is also able to persist security config.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationSource extends ConfigurationSource
{
    /**
     * Gets the current configuration.
     * 
     * @return the configuration, null if not loaded
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration getConfiguration();
    

    /**
     * Forces reloading the user configuration.
     * 
     * @return the configuration
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration loadConfiguration()
        throws ConfigurationException,
            IOException;
    

    /**
     * Returns the configuration that this configuration uses for defaulting.
     * 
     * @return a config source that is default source for this config or null
     */
    SecurityConfigurationSource getDefaultsSource();
}
