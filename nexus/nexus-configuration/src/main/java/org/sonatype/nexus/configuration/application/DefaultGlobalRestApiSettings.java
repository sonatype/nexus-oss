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

}
