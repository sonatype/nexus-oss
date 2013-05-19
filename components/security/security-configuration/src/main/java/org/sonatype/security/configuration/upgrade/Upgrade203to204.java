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
package org.sonatype.security.configuration.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.security.configuration.model.v2_0_3.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.security.configuration.model.v2_0_4.upgrade.BasicVersionUpgrade;

@Singleton
@Typed( SecurityConfigurationVersionUpgrader.class )
@Named( "2.0.3" )
public class Upgrade203to204
    implements SecurityConfigurationVersionUpgrader
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    
    private final int HASH_ITERATIONS = 1024;
    
    public Object loadConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException
    {
        Reader r = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            r = new BufferedReader(ReaderFactory.newXmlReader(file));

            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            return reader.read( r );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }
        finally
        {
            if ( r != null )
            {
                r.close();
            }
        }
    }

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
    	org.sonatype.security.configuration.model.v2_0_3.SecurityConfiguration oldc =
            (org.sonatype.security.configuration.model.v2_0_3.SecurityConfiguration) message.getConfiguration();

    	org.sonatype.security.configuration.model.SecurityConfiguration newc = new BasicVersionUpgrade().upgradeSecurityConfiguration( oldc );

        newc.setVersion( org.sonatype.security.configuration.model.SecurityConfiguration.MODEL_VERSION );
        newc.setHashIterations(HASH_ITERATIONS);
        message.setModelVersion( org.sonatype.security.configuration.model.SecurityConfiguration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

}
