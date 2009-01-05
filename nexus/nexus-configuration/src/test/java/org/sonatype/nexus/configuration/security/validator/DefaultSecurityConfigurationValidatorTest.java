/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.security.validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.validator.ValidationRequest;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

public class DefaultSecurityConfigurationValidatorTest
    extends AbstractNexusTestCase
{

    protected SecurityConfigurationValidator configurationValidator;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.configurationValidator = ( SecurityConfigurationValidator ) lookup( SecurityConfigurationValidator.ROLE );
    }

    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        NexusSecurityConfigurationXpp3Reader reader = new NexusSecurityConfigurationXpp3Reader();

        Reader fr = new InputStreamReader( is );

        return reader.read( fr );
    }

    public void testBad1()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/security/validator/security-bad1.xml" ) ) ) );

        assertFalse( response.isValid() );
        
        assertFalse( response.isModified() );

        // emails are not longer unique!
        assertEquals( 12, response.getValidationErrors().size() );
        
        assertEquals( 0, response.getValidationWarnings().size() );
    }

    public void testBad2()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/security/validator/security-bad2.xml" ) ) ) );

        assertFalse( response.isValid() );
        
        assertTrue( response.isModified() );

        assertEquals( 3, response.getValidationWarnings().size() );
        
        assertEquals( 11, response.getValidationErrors().size() );
    }
    
    public void testBad3()
        throws Exception
    {
        ValidationResponse response = configurationValidator.validateModel( new ValidationRequest(
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/nexus/configuration/security/validator/security-bad3.xml" ) ) ) );
    
        assertFalse( response.isValid() );
        
        assertTrue( response.isModified() );
    
        assertEquals( 3, response.getValidationWarnings().size() );
        
        assertEquals( 5, response.getValidationErrors().size() );
    }
}
