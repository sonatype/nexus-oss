/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.security.SecuritySystem;
import org.testng.Assert;

public class NexusConfigUtil
    extends ITUtil
{
    public NexusConfigUtil( AbstractNexusIntegrationTest test )
    {
        super( test );
    }

    private static Logger log = Logger.getLogger( NexusConfigUtil.class );

    public Configuration getNexusConfig()
        throws IOException
    {
        // TestContainer.getInstance().getContainer().addContextValue( "nexus-work",
        // AbstractNexusIntegrationTest.nexusWorkDir );
        NexusConfiguration config;
        try
        {
            config = getTest().getITPlexusContainer().lookup( NexusConfiguration.class );
            config.loadConfiguration( true );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
            throw new RuntimeException( e );
        }
        return config.getConfigurationModel();
    }

    public static File getNexusFile()
    {
        return new File( AbstractNexusIntegrationTest.WORK_CONF_DIR, "nexus.xml" );
    }

    public CPathMappingItem getRoute( String id )
        throws IOException
    {
        List<CPathMappingItem> routes = getNexusConfig().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> iter = routes.iterator(); iter.hasNext(); )
        {
            CPathMappingItem groupsSettingPathMappingItem = iter.next();

            if ( groupsSettingPathMappingItem.getId().equals( id ) )
            {
                return groupsSettingPathMappingItem;
            }

        }
        return null;
    }

    public void enableSecurity( boolean enabled )
        throws Exception
    {
        getTest().getITPlexusContainer().lookup( SecuritySystem.class ).setSecurityEnabled( enabled );
    }

    public M2LayoutedM1ShadowRepositoryConfiguration getRepoShadow( String repoId )
        throws IOException
    {
        List<CRepository> repos = getNexusConfig().getRepositories();

        for ( Iterator<CRepository> iter = repos.iterator(); iter.hasNext(); )
        {
            CRepository cRepo = iter.next();

            // check id
            if ( cRepo.getId().equals( repoId ) )
            {
                M2LayoutedM1ShadowRepositoryConfiguration exRepoConf =
                    new M2LayoutedM1ShadowRepositoryConfiguration( (Xpp3Dom) cRepo.getExternalConfiguration() );

                return exRepoConf;
            }
        }

        return null;
    }

    public CRepository getRepo( String repoId )
        throws IOException
    {
        List<CRepository> repos = getNexusConfig().getRepositories();

        for ( Iterator<CRepository> iter = repos.iterator(); iter.hasNext(); )
        {
            CRepository cRepo = iter.next();

            // check id
            if ( cRepo.getId().equals( repoId ) )
            {
                return cRepo;
            }
        }
        return null;
    }

    public void validateConfig()
        throws Exception
    {
        ApplicationConfigurationValidator validator =
            getTest().getITPlexusContainer().lookup( ApplicationConfigurationValidator.class );
        ValidationResponse vResponse = validator.validateModel( new ValidationRequest( getNexusConfig() ) );

        if ( !vResponse.isValid() )
        {
            throw new InvalidConfigurationException( vResponse );
        }

    }

    public M2GroupRepositoryConfiguration getGroup( String groupId )
        throws IOException
    {
        List<CRepository> repos = getNexusConfig().getRepositories();

        for ( Iterator<CRepository> iter = repos.iterator(); iter.hasNext(); )
        {
            CRepository cRepo = iter.next();

            // check id
            if ( cRepo.getId().equals( groupId ) )
            {
                M2GroupRepositoryConfiguration exRepoConf =
                    new M2GroupRepositoryConfiguration( (Xpp3Dom) cRepo.getExternalConfiguration() );

                return exRepoConf;
            }
        }

        return null;
    }

    public M2RepositoryConfiguration getM2Repo( String id )
        throws IOException
    {
        List<CRepository> repos = getNexusConfig().getRepositories();

        for ( Iterator<CRepository> iter = repos.iterator(); iter.hasNext(); )
        {
            CRepository cRepo = iter.next();

            // check id
            if ( cRepo.getId().equals( id ) )
            {
                M2RepositoryConfiguration exRepoConf =
                    new M2RepositoryConfiguration( (Xpp3Dom) cRepo.getExternalConfiguration() );

                return exRepoConf;
            }
        }

        return null;
    }
}
