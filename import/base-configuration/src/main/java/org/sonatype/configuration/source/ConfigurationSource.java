/**
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
package org.sonatype.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.configuration.Configuration;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;

/**
 * The Interface ConfigurationSource.
 * 
 * @author cstamas
 */
public interface ConfigurationSource<E extends Configuration>
{
    /**
     * Returns the validation response, if any. It is created on the loading of the user configuration.
     * 
     * @return the response or null if not applicable or config was still not loaded.
     */
    ValidationResponse getValidationResponse();

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
     * @throws ConfigurationException
     * @throws IOException
     */
    E getConfiguration();

    void setConfiguration( E configuration );

    /**
     * Forces reloading the user configuration.
     * 
     * @return the configuration
     * @throws ConfigurationException
     * @throws IOException
     */
    E loadConfiguration()
        throws ConfigurationException, IOException;

    /**
     * Returns the actual content of configuration as stream.
     * 
     * @return
     * @throws IOException
     */
    InputStream getConfigurationAsStream()
        throws IOException;

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
