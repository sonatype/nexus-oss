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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.security.realms.validator.ValidationResponse;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractConfigurationSource
    extends AbstractLogEnabled
    implements ConfigurationSource
{
    /** Flag to mark update. */
    private boolean configurationUpgraded;

    /** The validation response */
    private ValidationResponse validationResponse;
    
    public ValidationResponse getValidationResponse()
    {
        return validationResponse;
    }

    protected void setValidationResponse( ValidationResponse validationResponse )
    {
        this.validationResponse = validationResponse;
    }

    /**
     * Is configuration updated?
     */
    public boolean isConfigurationUpgraded()
    {
        return configurationUpgraded;
    }

    /**
     * Setter for configuration pugraded.
     * 
     * @param configurationUpgraded
     */
    public void setConfigurationUpgraded( boolean configurationUpgraded )
    {
        this.configurationUpgraded = configurationUpgraded;
    }

    /**
     * Returns the default source of ConfigurationSource. May be null.
     */
    public ConfigurationSource getDefaultsSource()
    {
        return null;
    }
}
