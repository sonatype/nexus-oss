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
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;

public abstract class AbstractWebSiteRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    public static final String WELCOME_FILES = "welcomeFiles";

    @Override
    protected void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        if ( externalConfiguration.getChild( WELCOME_FILES, false ) != null )
        {
            List<String> welcomeFiles = new ArrayList<String>( externalConfiguration
                .getChild( WELCOME_FILES ).getChildCount() );

            try
            {
                for ( PlexusConfiguration config : externalConfiguration.getChild( WELCOME_FILES ).getChildren() )
                {
                    welcomeFiles.add( config.getValue() );
                }
            }
            catch ( PlexusConfigurationException e )
            {
                throw new InvalidConfigurationException( "Cannot read configuration!" );
            }

            WebSiteRepository webSite = repository.adaptToFacet( WebSiteRepository.class );

            webSite.getWelcomeFiles().clear();

            webSite.getWelcomeFiles().addAll( welcomeFiles );
        }
    }
}
