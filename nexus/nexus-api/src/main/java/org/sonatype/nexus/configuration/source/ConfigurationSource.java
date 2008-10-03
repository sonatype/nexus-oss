/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
