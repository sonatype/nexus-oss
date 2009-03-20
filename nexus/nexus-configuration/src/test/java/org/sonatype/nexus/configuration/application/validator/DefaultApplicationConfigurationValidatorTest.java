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
package org.sonatype.nexus.configuration.application.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.upgrade.ApplicationConfigurationUpgrader;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.util.ExternalConfigUtil;

public class DefaultApplicationConfigurationValidatorTest
    extends AbstractNexusTestCase
{

    protected ApplicationConfigurationValidator configurationValidator;

    protected ApplicationConfigurationUpgrader configurationUpgrader;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.configurationValidator = (ApplicationConfigurationValidator) lookup( ApplicationConfigurationValidator.class );

        // I don't want to test both the validator, and the upgrade at the same time, but manually touching all these
        // config files is worse.
        this.configurationUpgrader = (ApplicationConfigurationUpgrader) lookup( ApplicationConfigurationUpgrader.class );
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );

    }

    protected Configuration loadNexusConfig( File configFile )
        throws Exception
    {
        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

        Reader fr = new FileReader( configFile );
        try
        {
            return reader.read( fr );
        }
        finally
        {
            IOUtil.close( fr );
        }

    }

    protected void saveConfiguration( Configuration config, String pathToConfig )
        throws IOException
    {
        NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

        Writer fw = null;
        try
        {
            fw = new FileWriter( pathToConfig );

            writer.write( fw, config );
        }
        finally
        {
            IOUtil.close( fw );
        }
    }

    public void testBad1()
        throws Exception
    {
        // get start with the default config
        this.copyDefaultConfigToPlace();
        Configuration config = this.loadNexusConfig( new File( this.getNexusConfiguration() ) );

        // make it bad

        // remove the name from a repository
        CRepository missingNameRepo = (CRepository) config.getRepositories().get( 0 );
        missingNameRepo.setName( null );

        // TDOD add 2 more warnings

        // wrong shadow type
        CRepository badShadow = new DefaultCRepository();
        badShadow.setId( "badShadow" );
        badShadow.setName( "Does not follow" );
        badShadow.setProviderRole( ShadowRepository.class.getName() );
        badShadow.setProviderHint( "m2-m1-shadow" );
        // Manipulate the dom
        Xpp3Dom externalConfig = new Xpp3Dom( "externalConfiguration" );
        badShadow.setExternalConfiguration( externalConfig );
        ExternalConfigUtil.setNodeValue( externalConfig, "masterRepositoryId", "non-existent" );
        config.addRepository( badShadow );

        // now validate it
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest( config ) );

        assertEquals( 3, response.getValidationWarnings().size() );

        assertEquals( 1, response.getValidationErrors().size() );

        // codehaus-snapshots has no name, it will be defaulted
        assertTrue( response.isModified() );

        assertFalse( response.isValid() );
    }

    public void testBad2()
        throws Exception
    {
        // get start with the default config
        this.copyDefaultConfigToPlace();
        Configuration config = this.loadNexusConfig( new File( this.getNexusConfiguration() ) );

        // make it bad

        // invalid policy
        CRepository invalidPolicyRepo = (CRepository) config.getRepositories().get( 0 );
        Xpp3Dom externalConfig = (Xpp3Dom) invalidPolicyRepo.getExternalConfiguration();
        ExternalConfigUtil.setNodeValue( externalConfig, "repositoryPolicy", "badPolicy" );

        // duplicate the repository id
        for ( CRepository repo : (List<CRepository>) config.getRepositories() )
        {
            if ( !repo.getId().equals( "central" ) )
            {
                // duplicate
                repo.setId( "central" );
                break;
            }
        }

        // TODO: add more errors here

        // now validate it
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest( config ) );

        assertFalse( response.isValid() );

        assertFalse( response.isModified() );

        assertEquals( 6, response.getValidationErrors().size() );

        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testNexus1710Bad()
        throws Exception
    {

        // this one is easy because you can compare:
        // /org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml.result-bad
        // with
        // /org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml.result
        // and you have the diff, and you already have to manually update the good one.

        // this was before fix: groupId/repoId name clash
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml.result-bad" ) ) ) );

        assertFalse( response.isValid() );

        assertFalse( response.isModified() );

        assertEquals( 1, response.getValidationErrors().size() );

        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testNexus1710Good()
        throws Exception
    {
        // this is after fix: groupId is appended by "-group" to resolve clash
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml.result" ) ) ) );

        assertTrue( response.isValid() );

        assertFalse( response.isModified() );
    }
}
