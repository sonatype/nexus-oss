/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.model.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.UnsupportedConfigurationVersionException;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.security.model.Configuration;

/**
 * Default configuration updater, using versioned Modello models. It tried to detect version signature from existing
 * file and apply apropriate modello io stuff to load configuration. It is also aware of changes across model versions.
 * 
 * @author cstamas
 */
@Singleton
@Typed( SecurityConfigurationUpgrader.class )
@Named( "default" )
public class DefaultSecurityConfigurationUpgrader
    implements SecurityConfigurationUpgrader
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<String, SecurityUpgrader> upgraders;

    private final Map<String, SecurityDataUpgrader> dataUpgraders;

    @Inject
    public DefaultSecurityConfigurationUpgrader( Map<String, SecurityUpgrader> upgraders,
                                                 Map<String, SecurityDataUpgrader> dataUpgraders )
    {
        this.upgraders = upgraders;
        this.dataUpgraders = dataUpgraders;
    }

    /**
     * This implementation relies to plexus registered upgraders. It will cycle through them until the configuration is
     * the needed (current) model version.
     * 
     * @throws
     */
    public Configuration loadOldConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException, UnsupportedConfigurationVersionException
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

        SecurityUpgrader upgrader = upgraders.get( msg.getModelVersion() );

        if ( upgrader != null )
        {
            logger.info( "Upgrading old Security configuration file (version " + msg.getModelVersion() + ") from "
                + file.getAbsolutePath() );

            msg.setConfiguration( upgrader.loadConfiguration( file ) );

            while ( !Configuration.MODEL_VERSION.equals( msg.getModelVersion() ) )
            {

                // an application might need to upgrade content, that is NOT part of the model
                SecurityDataUpgrader dataUpgrader = this.dataUpgraders.get( msg.getModelVersion() );

                if ( upgrader != null )
                {
                    upgrader.upgrade( msg );

                    if ( dataUpgrader != null )
                    {
                        dataUpgrader.upgrade( msg.getConfiguration() );
                    }
                }
                else
                {
                    // we could parse the XML but have no model version? Is this security config at all?
                    throw new UnsupportedConfigurationVersionException( modelVersion, file );
                }

                upgrader = upgraders.get( msg.getModelVersion() );
            }

            logger.info( "Security configuration file upgraded to current version " + msg.getModelVersion()
                + " succesfully." );

            return (Configuration) msg.getConfiguration();
        }
        else
        {
            // we could parse the XML but have no model version? Is this security config at all?
            throw new UnsupportedConfigurationVersionException( modelVersion, file );
        }
    }
}
