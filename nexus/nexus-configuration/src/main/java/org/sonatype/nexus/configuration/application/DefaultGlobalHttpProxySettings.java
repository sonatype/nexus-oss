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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CGlobalHttpProxySettingsCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;

@Component( role = GlobalHttpProxySettings.class )
public class DefaultGlobalHttpProxySettings
    extends AbstractConfigurable
    implements GlobalHttpProxySettings
{
    @Requirement
    private AuthenticationInfoConverter authenticationInfoConverter;

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
    protected CRemoteHttpProxySettings getCurrentConfiguration( boolean forWrite )
    {
        return ( (CGlobalHttpProxySettingsCoreConfiguration) getCurrentCoreConfiguration() )
            .getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CGlobalHttpProxySettingsCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    // ==

    public boolean isBlockInheritance()
    {
        if ( isEnabled() )
        {
            return getCurrentConfiguration( false ).isBlockInheritance();
        }
        
        return false;
    }

    public void setBlockInheritance( boolean val )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setBlockInheritance( val );
    }

    public String getHostname()
    {
        if ( isEnabled() )
        {
            return getCurrentConfiguration( false ).getProxyHostname();
        }
        
        return null;
    }

    public void setHostname( String hostname )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setProxyHostname( hostname );
    }

    public int getPort()
    {
        if ( isEnabled() )
        {
            return getCurrentConfiguration( false ).getProxyPort();
        }
        
        return -1;
    }

    public void setPort( int port )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setProxyPort( port );
    }

    public RemoteAuthenticationSettings getProxyAuthentication()
    {
        if ( isEnabled() )
        {
            try
            {
                return authenticationInfoConverter.convertAndValidateFromModel( getCurrentConfiguration( false )
                    .getAuthentication() );
            }
            catch ( ConfigurationException e )
            {
                // FIXME: what here??
    
                setProxyAuthentication( null );
    
                return null;
            }
        }
        
        return null;
    }

    public void setProxyAuthentication( RemoteAuthenticationSettings proxyAuthentication )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setAuthentication(
                                                           authenticationInfoConverter
                                                               .convertToModel( proxyAuthentication ) );
    }

    public RemoteProxySettings convertAndValidateFromModel( CRemoteHttpProxySettings model )
        throws ConfigurationException
    {
        ( (CGlobalHttpProxySettingsCoreConfiguration) getCurrentCoreConfiguration() ).doValidateChanges( model );

        if ( model != null )
        {
            RemoteProxySettings remoteProxySettings = new DefaultRemoteProxySettings();

            remoteProxySettings.setBlockInheritance( model.isBlockInheritance() );

            if ( remoteProxySettings.isBlockInheritance() )
            {
                return remoteProxySettings;
            }

            remoteProxySettings.setHostname( model.getProxyHostname() );

            remoteProxySettings.setPort( model.getProxyPort() );

            remoteProxySettings.setProxyAuthentication( authenticationInfoConverter.convertAndValidateFromModel( model
                .getAuthentication() ) );
            
            remoteProxySettings.setNonProxyHosts( new HashSet<String>( model.getNonProxyHosts() ) );

            return remoteProxySettings;
        }
        else
        {
            return null;
        }
    }

    public CRemoteHttpProxySettings convertToModel( RemoteProxySettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteHttpProxySettings model = new CRemoteHttpProxySettings();

            model.setBlockInheritance( settings.isBlockInheritance() );

            model.setProxyHostname( settings.getHostname() );

            model.setProxyPort( settings.getPort() );

            model.setAuthentication( authenticationInfoConverter.convertToModel( settings.getProxyAuthentication() ) );
            
            model.setNonProxyHosts( new ArrayList<String>(settings.getNonProxyHosts() ) );

            return model;
        }
    }

    // ==

    public void disable()
    {
        ( (CGlobalHttpProxySettingsCoreConfiguration) getCurrentCoreConfiguration() ).nullifyConfig();
    }

    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ) != null;
    }
    
    protected void initConfig()
    {
        ( (CGlobalHttpProxySettingsCoreConfiguration) getCurrentCoreConfiguration() ).initConfig();
    }

    public String getName()
    {
        return "Global Http Proxy Settings";
    }

    public Set<String> getNonProxyHosts()
    {
        if ( isEnabled() )
        {
            return new HashSet<String>( getCurrentConfiguration( false ).getNonProxyHosts() );
        }
        
        return Collections.emptySet();
    }

    public void setNonProxyHosts( Set<String> nonProxyHosts )
    {
        if ( !isEnabled() )
        {
            initConfig();
        }

        getCurrentConfiguration( true ).setNonProxyHosts( new ArrayList<String>( nonProxyHosts ) );
    }
}