/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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

        this.configurationValidator = (ApplicationConfigurationValidator) lookup( ApplicationConfigurationValidator.ROLE );
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
}
