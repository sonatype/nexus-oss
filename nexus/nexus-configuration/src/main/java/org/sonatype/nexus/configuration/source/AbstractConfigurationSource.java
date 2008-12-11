/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.source;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

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
