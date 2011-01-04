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
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteConnectionSettingsChangedEvent;
import org.sonatype.nexus.configuration.model.CGlobalRemoteConnectionSettingsCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;

@Component( role = GlobalRemoteConnectionSettings.class )
public class DefaultGlobalRemoteConnectionSettings
    extends AbstractConfigurable
    implements GlobalRemoteConnectionSettings
{
    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return null;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected CRemoteConnectionSettings getCurrentConfiguration( boolean forWrite )
    {
        return ( (CGlobalRemoteConnectionSettingsCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CGlobalRemoteConnectionSettingsCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    // ==

    public int getConnectionTimeout()
    {
        return getCurrentConfiguration( false ).getConnectionTimeout();
    }

    public void setConnectionTimeout( int connectionTimeout )
    {
        getCurrentConfiguration( true ).setConnectionTimeout( connectionTimeout );
    }

    public String getQueryString()
    {
        return getCurrentConfiguration( false ).getQueryString();
    }

    public void setQueryString( String queryString )
    {
        getCurrentConfiguration( true ).setQueryString( queryString );
    }

    public int getRetrievalRetryCount()
    {
        return getCurrentConfiguration( false ).getRetrievalRetryCount();
    }

    public void setRetrievalRetryCount( int retrievalRetryCount )
    {
        getCurrentConfiguration( true ).setRetrievalRetryCount( retrievalRetryCount );
    }

    public String getUserAgentCustomizationString()
    {
        return getCurrentConfiguration( false ).getUserAgentCustomizationString();
    }

    public void setUserAgentCustomizationString( String userAgentCustomizationString )
    {
        getCurrentConfiguration( true ).setUserAgentCustomizationString( userAgentCustomizationString );
    }

    // ==

    public RemoteConnectionSettings convertAndValidateFromModel( CRemoteConnectionSettings model )
        throws ConfigurationException
    {
        ( (CGlobalRemoteConnectionSettingsCoreConfiguration) getCurrentCoreConfiguration() ).doValidateChanges( model );

        if ( model != null )
        {
            RemoteConnectionSettings remoteConnectionSettings = new DefaultRemoteConnectionSettings();

            remoteConnectionSettings.setConnectionTimeout( model.getConnectionTimeout() );

            remoteConnectionSettings.setQueryString( model.getQueryString() );

            remoteConnectionSettings.setRetrievalRetryCount( model.getRetrievalRetryCount() );

            remoteConnectionSettings.setUserAgentCustomizationString( model.getUserAgentCustomizationString() );

            return remoteConnectionSettings;
        }
        else
        {
            return null;
        }
    }

    public CRemoteConnectionSettings convertToModel( RemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteConnectionSettings model = new CRemoteConnectionSettings();

            model.setConnectionTimeout( settings.getConnectionTimeout() );

            model.setQueryString( settings.getQueryString() );

            model.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

            model.setUserAgentCustomizationString( settings.getUserAgentCustomizationString() );

            return model;
        }
    }

    public String getName()
    {
        return "Global Remote Connection Settings";
    }

    @Override
    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean wasDirty = super.commitChanges();

        if ( wasDirty )
        {
            getApplicationEventMulticaster().notifyEventListeners( new GlobalRemoteConnectionSettingsChangedEvent( this ) );
        }

        return wasDirty;
    }

}
