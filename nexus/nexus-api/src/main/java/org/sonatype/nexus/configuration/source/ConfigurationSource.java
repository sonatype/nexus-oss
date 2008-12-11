/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.configuration.validator.ValidationResponse;

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
