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
package org.sonatype.nexus.configuration.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * Default configuration updater, using versioned Modello models. It tried to detect version signature from existing
 * file and apply apropriate modello io stuff to load configuration. It is also aware of changes across model versions.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultConfigurationUpgrader
    extends AbstractLogEnabled
    implements ConfigurationUpgrader
{
    /**
     * @plexus.requirement role="org.sonatype.nexus.configuration.upgrade.Upgrader"
     */
    private Map<String, Upgrader> upgraders;

    /**
     * This implementation relies to plexus registered upgraders. It will cycle through them until the configuration is
     * the needed (current) model version.
     */
    public Configuration loadOldConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException,
            UnsupportedConfigurationVersionException
    {
        // try to find out the model version
        String modelVersion = null;

        try
        {
            Reader r = new FileReader( file );

            Xpp3Dom dom = Xpp3DomBuilder.build( r );

            modelVersion = dom.getChild( "version" ).getValue();
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }

        if ( Configuration.MODEL_VERSION.equals( modelVersion ) )
        {
            // we have a problem here, model version is OK but we could not load it previously?
            throw new ConfigurationIsCorruptedException( file );
        }

        UpgradeMessage msg = new UpgradeMessage();
        
        msg.setModelVersion( modelVersion );

        // a correction for SWX
        if ( "1.0".equals( modelVersion ) )
        {
            msg.setModelVersion( "1.0.0" );
        }

        Upgrader upgrader = upgraders.get( msg.getModelVersion() );

        if ( upgrader != null )
        {
            getLogger().info( "Loading OLD configuration from " + file.getAbsolutePath() );

            msg.setConfiguration( upgrader.loadConfiguration( file ) );

            while ( !Configuration.MODEL_VERSION.equals( msg.getModelVersion() ) )
            {
                if ( upgrader != null )
                {
                    getLogger().info( "Upgrading OLD configuration from version " + msg.getModelVersion() );

                    upgrader.upgrade( msg );
                }
                else
                {
                    // we could parse the XML but have no model version? Is this nexus config at all?
                    throw new UnsupportedConfigurationVersionException( modelVersion, file );
                }

                upgrader = upgraders.get( msg.getModelVersion() );
            }

            return (Configuration) msg.getConfiguration();
        }
        else
        {
            // we could parse the XML but have no model version? Is this nexus config at all?
            throw new UnsupportedConfigurationVersionException( modelVersion, file );
        }
    }
}
