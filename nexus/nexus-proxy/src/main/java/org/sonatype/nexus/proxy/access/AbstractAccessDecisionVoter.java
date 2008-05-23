/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
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
