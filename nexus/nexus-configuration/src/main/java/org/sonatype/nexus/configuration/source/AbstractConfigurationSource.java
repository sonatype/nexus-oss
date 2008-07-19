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
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractConfigurationSource
    extends AbstractLogEnabled
    implements ConfigurationSource
{

    /**
     * The application interpolation provider.
     * 
     * @plexus.requirement
     */
    private ApplicationInterpolatorProvider interpolatorProvider;

    /** Flag to mark update. */
    private boolean configurationUpgraded;

    /** The configuration. */
    private Configuration configuration;

    /** The validation response */
    private ValidationResponse validationResponse;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }

    public ValidationResponse getValidationResponse()
    {
        return validationResponse;
    }

    protected void setValidationResponse( ValidationResponse validationResponse )
    {
        this.validationResponse = validationResponse;
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
    @SuppressWarnings( "unchecked" )
    protected void loadConfiguration( InputStream is )
        throws IOException
    {
        setConfigurationUpgraded( false );

        Reader fr = null;

        try
        {
            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            InterpolatorFilterReader ip = new InterpolatorFilterReader( fr, interpolatorProvider );

            // read again with interpolation
            configuration = reader.read( ip );
        }
        catch ( XmlPullParserException e )
        {
            rejectConfiguration( "Nexus configuration file was not loaded, it has the wrong structure." );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Nexus.xml is broken:", e );
            }
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
        
        // check the model version if loaded
        if ( configuration != null && !Configuration.MODEL_VERSION.equals( configuration.getVersion() ) )
        {
            rejectConfiguration( "Nexus configuration file was loaded but discarded, it has the wrong version number." );
        }
        
        if ( getConfiguration() != null )
        {
            getLogger().info( "Configuration loaded succesfully." );
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void saveConfiguration( OutputStream os, Configuration configuration )
        throws IOException
    {
        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( os );

            NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
    }

    /**
     * Is configuration updated?
     */
    public boolean isConfigurationUpgraded()
    {
        return configurationUpgraded;
    }

    /**
     * Setter for configuration pugraded.
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
    public ConfigurationSource getDefaultsSource()
    {
        return null;
    }

}
