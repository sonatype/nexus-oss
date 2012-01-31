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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.Configuration;
import org.sonatype.configuration.validation.ValidationResponse;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractStreamConfigurationSource<E extends Configuration>
    implements ConfigurationSource<E>
{
    /** The configuration. */
    private E configuration;

    /** Flag to mark update. */
    private boolean configurationUpgraded;

    /** The validation response */
    private ValidationResponse validationResponse;

    private Logger logger = LoggerFactory.getLogger( this.getClass() );

    public E getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( E configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Called by subclasses when loaded configuration is rejected for some reason.
     */
    protected void rejectConfiguration( String message )
    {
        this.configuration = null;

        if ( message != null )
        {
            getLogger().warn( message );
        }
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected abstract void loadConfiguration( InputStream is )
        throws IOException;

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
     * Setter for configuration upgraded.
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
    public ConfigurationSource<?> getDefaultsSource()
    {
        return null;
    }

    public Logger getLogger()
    {
        return this.logger;
    }
}
