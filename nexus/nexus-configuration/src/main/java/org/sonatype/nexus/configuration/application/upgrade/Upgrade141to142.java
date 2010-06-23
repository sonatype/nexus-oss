/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_4_2.upgrade.BasicVersionConverter;

/**
 * Upgrades configuration model from version 1.4.1 to 1.4.2.
 * 
 * @author bdemers
 */
@Component( role = SingleVersionUpgrader.class, hint = "1.4.1" )
public class Upgrade141to142
    extends AbstractLogEnabled
    implements SingleVersionUpgrader
{

    public Object loadConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        org.sonatype.nexus.configuration.model.v1_4_1.Configuration conf = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            org.sonatype.nexus.configuration.model.v1_4_1.io.xpp3.NexusConfigurationXpp3Reader reader =
                new org.sonatype.nexus.configuration.model.v1_4_1.io.xpp3.NexusConfigurationXpp3Reader();

            conf = reader.read( fr );
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

        return conf;
    }

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        org.sonatype.nexus.configuration.model.v1_4_1.Configuration oldc =
            (org.sonatype.nexus.configuration.model.v1_4_1.Configuration) message.getConfiguration();

        org.sonatype.nexus.configuration.model.Configuration newc =
            new BasicVersionConverter().convertConfiguration( oldc );

        // fix for NEXUS-3604
        for ( org.sonatype.nexus.configuration.model.CRepository repository : newc.getRepositories() )
        {
            if ( "org.sonatype.nexus.proxy.repository.GroupRepository".equals( repository.getProviderRole() ) )
            {
                repository.setSearchable( false );
            }
        }

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

}
