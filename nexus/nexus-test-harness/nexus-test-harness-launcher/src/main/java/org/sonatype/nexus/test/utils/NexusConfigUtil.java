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
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.security.SecuritySystem;

public class NexusConfigUtil
{

    private static Logger log = Logger.getLogger( NexusConfigUtil.class );

    public static Configuration getNexusConfig()
        throws IOException
    {
        // TestContainer.getInstance().getContainer().addContextValue( "nexus-work",
        // AbstractNexusIntegrationTest.nexusWorkDir );
        NexusConfiguration config;
        try
        {
            config = TestContainer.getInstance().lookup( NexusConfiguration.class );
            config.loadConfiguration( true );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
            Assert.fail( "Unable to load config " + e.getMessage() );
            config = null;
        }
        return config.getConfiguration();
    }

    private static void saveConfig( Configuration config )
        throws IOException
    {
        Writer fw = null;
        try
        {
            FileOutputStream fos = new FileOutputStream( getNexusFile() );
            fw = new OutputStreamWriter( fos );

            NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

            writer.write( fw, config );
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

    public static File getNexusFile()
    {
        return new File( AbstractNexusIntegrationTest.WORK_CONF_DIR, "nexus.xml" );
    }

    @SuppressWarnings( "unchecked" )
    public static CPathMappingItem getRoute( String id )
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

    public static void enableSecurity( boolean enabled )
        throws Exception
    {
        TestContainer.getInstance().lookup( SecuritySystem.class ).setSecurityEnabled( enabled );
    }

    public static M2LayoutedM1ShadowRepositoryConfiguration getRepoShadow( String repoId )
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

    public static CRepository getRepo( String repoId )
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

    public static void validateConfig()
        throws Exception
    {
        ApplicationConfigurationValidator validator =
            TestContainer.getInstance().lookup( ApplicationConfigurationValidator.class );
        ValidationResponse vResponse = validator.validateModel( new ValidationRequest( getNexusConfig() ) );

        if ( !vResponse.isValid() )
        {
            throw new InvalidConfigurationException( vResponse );
        }

    }

    public static M2GroupRepositoryConfiguration getGroup( String groupId )
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

    public static M2RepositoryConfiguration getM2Repo( String id )
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
