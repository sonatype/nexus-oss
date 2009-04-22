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
import java.io.InputStream;

import org.sonatype.jsecurity.realms.validator.ValidationResponse;

/**
 * The Interface ConfigurationSource.
 * 
 * @author cstamas
 */
public interface ConfigurationSource
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
     * Returns the actual content of user configuration as stream.
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
