package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRestApiSettingsCoreConfiguration;

@Component( role = RestApiConfiguration.class )
public class DefaultRestApiConfiguration
    extends AbstractConfigurable
    implements RestApiConfiguration
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

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
    protected CRestApiSettings getCurrentConfiguration( boolean forWrite )
    {
        return ( (CRestApiSettingsCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CRestApiSettingsCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    // ==

    public boolean isForceBaseUrl()
    {
        return getCurrentConfiguration( false ).isForceBaseUrl();
    }

    public void setForceBaseUrl( boolean forceBaseUrl )
    {
        getCurrentConfiguration( true ).setForceBaseUrl( forceBaseUrl );
    }

    public String getBaseUrl()
    {
        return getCurrentConfiguration( false ).getBaseUrl();
    }

    public void setBaseUrl( String baseUrl )
    {
        getCurrentConfiguration( true ).setBaseUrl( baseUrl );
    }

    public int getSessionExpiration()
    {
        return getCurrentConfiguration( false ).getSessionExpiration();
    }

    public void setSessionExpiration( int sessionExpiration )
    {
        getCurrentConfiguration( true ).setSessionExpiration( sessionExpiration );
    }
}
