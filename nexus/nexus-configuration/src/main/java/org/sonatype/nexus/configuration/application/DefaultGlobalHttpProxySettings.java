package org.sonatype.nexus.configuration.application;

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
}