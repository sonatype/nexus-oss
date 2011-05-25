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

    protected void initConfig()
    {
        ( (CGlobalRestApiCoreConfiguration) getCurrentCoreConfiguration() ).initConfig();
    }

    @Override
    public String getName()
    {
        return "Global Rest Api Settings";
    }

    // ==

    @Override
    public void disable()
    {
        ( (CGlobalRestApiCoreConfiguration) getCurrentCoreConfiguration() ).nullifyConfig();
    }

    @Override
    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ) != null;
    }

    @Override
    public void setForceBaseUrl( boolean forceBaseUrl )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setForceBaseUrl( forceBaseUrl );
    }

    @Override
    public boolean isForceBaseUrl()
    {
        if ( !isEnabled() )
        {
            return false;
        }
        
        return getCurrentConfiguration( false ).isForceBaseUrl();
    }

    @Override
    public void setBaseUrl( String baseUrl )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setBaseUrl( baseUrl );
    }

    @Override
    public String getBaseUrl()
    {
        if ( !isEnabled() )
        {
            return null;
        }
        
        return getCurrentConfiguration( false ).getBaseUrl();
    }

    @Override
    public void setUITimeout( int uiTimeout )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setUiTimeout( uiTimeout );
    }

    @Override
    public int getUITimeout()
    {
        if ( !isEnabled() )
        {
            return 0;
        }

        return getCurrentConfiguration( false ).getUiTimeout();
    }

}
