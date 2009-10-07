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
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_0_8.CProps;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepository;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask;
import org.sonatype.nexus.configuration.model.v1_0_8.CSecurity;
import org.sonatype.nexus.configuration.model.v1_0_8.upgrade.BasicVersionUpgrade;

@Component( role = SingleVersionUpgrader.class, hint = "1.0.7" )
public class Upgrade107to108
    extends AbstractLogEnabled
    implements SingleVersionUpgrader
{
    private BasicVersionUpgrade converter = new BasicVersionUpgrade()
    {
        @Override
        public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(
            org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup repositoryGroup,
            org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup value )
        {
            org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup group = super.upgradeCRepositoryGroup(
                repositoryGroup,
                value );
            group.setType( "maven2" );
            return group;
        }

        @Override
        public CSecurity upgradeCSecurity( org.sonatype.nexus.configuration.model.v1_0_7.CSecurity security,
            CSecurity value )
        {
            org.sonatype.nexus.configuration.model.v1_0_8.CSecurity newSecurity = super.upgradeCSecurity( security, value );
            newSecurity.removeRealm( "NexusMethodAuthorizingRealm" );
            newSecurity.removeRealm( "NexusTargetAuthorizingRealm" );
            newSecurity.removeRealm( "XmlMethodAuthorizingRealm" );
            newSecurity.addRealm( "XmlAuthorizingRealm" );

            return newSecurity;
        }
    };

    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        org.sonatype.nexus.configuration.model.v1_0_7.Configuration conf = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader reader = new org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader();

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
    {
       org.sonatype.nexus.configuration.model.v1_0_7.Configuration oldc = (org.sonatype.nexus.configuration.model.v1_0_7.Configuration) message
            .getConfiguration();

        org.sonatype.nexus.configuration.model.v1_0_8.Configuration newc = converter.upgradeConfiguration( oldc );

        // NEXUS-1710: enforce ID uniqueness, but also /content URL must remain unchanged for existing systems.
        // sadly, as part of upgrade, we must ensure the repoId uniqueness, which was not case in pre-1.3 nexuses
        List<String> repoIds = new ArrayList<String>();

        // repoIds are _unchanged_
        if ( newc.getRepositories() != null )
        {
            for ( CRepository repository : (List<CRepository>) newc.getRepositories() )
            {
                // need to check case insensitive for windows
                repoIds.add( repository.getId().toLowerCase() );
            }
        }

        // shadowIds are _unchanged_ (the repo:shadow ID uniqueness was enforced in pre-1.3!)
        if ( newc.getRepositoryShadows() != null )
        {
            for ( CRepositoryShadow repository : (List<CRepositoryShadow>) newc.getRepositoryShadows() )
            {
                // need to check case insensitive for windows
                repoIds.add( repository.getId().toLowerCase() );
            }
        }

        if ( newc.getRepositoryGrouping() != null && newc.getRepositoryGrouping().getRepositoryGroups() != null )
        {
            for ( CRepositoryGroup group : (List<CRepositoryGroup>) newc.getRepositoryGrouping().getRepositoryGroups() )
            {
                // need to check case insensitive for windows
                if ( repoIds.contains( group.getGroupId().toLowerCase() ) )
                {
                    String groupId = group.getGroupId();
                    // if duped only
                    group.setPathPrefix( groupId );
                    group.setGroupId( groupId + "-group" );

                    upgradeGroupRefsInTask( newc, groupId );
                }
            }
        }

        newc.setVersion( org.sonatype.nexus.configuration.model.v1_0_8.Configuration.MODEL_VERSION );
        message.setModelVersion( org.sonatype.nexus.configuration.model.v1_0_8.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
        
    }
    
    private void upgradeGroupRefsInTask( org.sonatype.nexus.configuration.model.v1_0_8.Configuration conf, String groupId )
    {
        for ( CScheduledTask task : (List<CScheduledTask>) conf.getTasks() )
        {
            for ( CProps prop : (List<CProps>) task.getProperties() )
            {
                if ( prop.getKey().equals( "repositoryOrGroupId" ) && prop.getValue().equals( "group_" + groupId ) )
                {
                    prop.setValue( "group_" + groupId + "-group" );
                }
            }
        }
    }
}
