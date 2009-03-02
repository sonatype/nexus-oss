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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

public class DefaultApplicationConfigurationValidatorTest
    extends AbstractNexusTestCase
{

    protected ApplicationConfigurationValidator configurationValidator;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.configurationValidator = (ApplicationConfigurationValidator) lookup( ApplicationConfigurationValidator.class );
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );

    }

    public void testBad1()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/validator/nexus-bad1.xml" ) ) ) );

        assertFalse( response.isValid() );

        // codehaus-snapshots has no name, it will be defaulted
        assertTrue( response.isModified() );

        assertEquals( 1, response.getValidationErrors().size() );

        assertEquals( 3, response.getValidationWarnings().size() );
    }

    public void testBad2()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/validator/nexus-bad2.xml" ) ) ) );

        assertFalse( response.isValid() );

        assertFalse( response.isModified() );

        assertEquals( 6, response.getValidationErrors().size() );

        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testNexus1710Bad()
        throws Exception
    {
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
