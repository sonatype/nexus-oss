package org.sonatype.nexus.proxy.access;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

public abstract class AbstractAccessDecisionVoter
    extends LoggingComponent
    implements AccessDecisionVoter, Initializable, ConfigurationChangeListener
{
    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * @plexus.requirement
     */
    private ApplicationInterpolatorProvider applicationInterpolatorProvider;

    private File configurationDir;

    private Map<String, String> configuration = new HashMap<String, String>();

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addConfigurationChangeListener( this );

        configurationDir = applicationConfiguration.getConfigurationDirectory();
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        configurationDir = applicationConfiguration.getConfigurationDirectory();
    }

    public void setConfiguration( Map<String, String> config )
    {
        configuration.clear();

        for ( String key : config.keySet() )
        {
            String interpolated = applicationInterpolatorProvider.interpolate( config.get( key ), "" );

            configuration.put( key, interpolated );
        }
    }

    protected File getConfigurationDir()
    {
        return configurationDir;
    }

    protected String getConfigurationValue( String key )
    {
        return getConfigurationValue( key, null );
    }

    protected String getConfigurationValue( String key, String def )
    {
        if ( configuration.containsKey( key ) )
        {
            return configuration.get( key );
        }
        else
        {
            return def;
        }
    }
}
