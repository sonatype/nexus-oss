/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
import java.util.Iterator;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.v2_0_2.io.xpp3.SecurityConfigurationXpp3Reader;
import org.sonatype.security.model.v2_0_3.upgrade.BasicVersionUpgrade;

@Component( role = SecurityUpgrader.class, hint = "2.0.2" )
public class Upgrade202to203
    implements SecurityUpgrader
{
    private static String DEFAULT_SOURCE = "default";

    @Requirement
    private Logger logger;
    
    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

            return reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
    }

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        org.sonatype.security.model.v2_0_2.Configuration oldc = ( org.sonatype.security.model.v2_0_2.Configuration ) message.getConfiguration();

        org.sonatype.security.model.Configuration newc = new BasicVersionUpgrade().upgradeConfiguration( oldc );

        // now strip out all the unused role mappings
        
        for ( Iterator<CUserRoleMapping> iter = newc.getUserRoleMappings().iterator(); iter.hasNext() ;  )
        {
            CUserRoleMapping roleMapping = iter.next();
            
            if( DEFAULT_SOURCE.equalsIgnoreCase( roleMapping.getSource() ) && !this.hasUser( roleMapping.getUserId(), newc ) )
            {
                logger.info( "Removing orphaned user role mapping for user: '"+ roleMapping.getUserId() + "'." );
                iter.remove();
            }
        }
        
        newc.setVersion( org.sonatype.security.model.Configuration.MODEL_VERSION );
        message.setModelVersion( org.sonatype.security.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }
    
    private boolean hasUser( String userId, Configuration configuration )
    {
        for ( CUser user : configuration.getUsers() )
        {
            if( user.getId().equals( userId ))
            {
                return true;
            }
        }
        return false;
    }

}
