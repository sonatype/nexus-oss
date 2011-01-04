/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CGlobalRestApiCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRestApiSettings;

@Component( role = GlobalRestApiSettings.class )
public class DefaultGlobalRestApiSettings
    extends AbstractConfigurable
    implements GlobalRestApiSettings
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    public void disable()
    {
        ( (CGlobalRestApiCoreConfiguration) getCurrentCoreConfiguration() ).nullifyConfig();
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }
    
    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }

    @Override
    protected CRestApiSettings getCurrentConfiguration( boolean forWrite )
    {
        return ( (CGlobalRestApiCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CGlobalRestApiCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    public String getBaseUrl()
    {
        return getCurrentConfiguration( false ).getBaseUrl();
    }

    public boolean isForceBaseUrl()
    {
        return getCurrentConfiguration( false ).isForceBaseUrl();
    }

    public void setBaseUrl( String baseUrl )
    {
        if ( !isEnabled() )
        {
            this.initConfig();
        }

        getCurrentConfiguration( true ).setBaseUrl( baseUrl );

    }

    public void setForceBaseUrl( boolean forceBaseUrl )
    {
        if ( !isEnabled() )
        {
            this.initConfig();
        }

        getCurrentConfiguration( true ).setForceBaseUrl( forceBaseUrl );
    }

    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ) != null;
    }

    protected void initConfig()
    {
        ( (CGlobalRestApiCoreConfiguration) getCurrentCoreConfiguration() ).initConfig();
    }

    public String getName()
    {
        return "Global Rest Api Settings";
    }

    public void setUITimeout( int uiTimeout )
    {
        getCurrentConfiguration( true ).setUiTimeout( uiTimeout );
    }

    public int getUITimeout()
    {
        return getCurrentConfiguration( false ).getUiTimeout();
    }

}
