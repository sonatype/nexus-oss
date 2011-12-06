/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.capabilities.internal.config;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.junit.Test;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfigurationValidator;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;

@SuppressWarnings( { "unchecked", "deprecation" } )
public class DefaultCapabilityConfigurationValidatorTest
    extends AbstractNexusTestCase
{

    private CapabilityConfigurationValidator validator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        validator = lookup( CapabilityConfigurationValidator.class );
    }

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration(configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }


    @Test
    public void testPassValidate()
    {
        CCapability cap = createValidCapability();

        ValidationResponse res = validator.validate( cap, true );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertTrue( res.getValidationErrors().isEmpty() );
    }

    private CCapability createValidCapability()
    {
        CCapability cap = new CCapability();
        cap.setId( "0x00AABB" );
        cap.setDescription( "UnitTest" );
        cap.setTypeId( "AnyType" );
        CCapabilityProperty prop = new CCapabilityProperty();
        prop.setKey( "key" );
        prop.setValue( "value" );
        cap.addProperty( prop );
        return cap;
    }

    @Test
    public void testFailValidate()
    {
        CCapability cap = new CCapability();
        CCapabilityProperty prop = new CCapabilityProperty();
        cap.addProperty( prop );

        ValidationResponse res = validator.validate( cap, false );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertEquals( 3, res.getValidationErrors().size() );
    }

    @Test
    public void testPassValidateModel()
    {
        Configuration cfg = new Configuration();
        cfg.addCapability( createValidCapability() );
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( cfg );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertTrue( res.getValidationErrors().isEmpty() );
    }

    @Test
    public void testFailValidateModel()
    {
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( null );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertEquals( 1, res.getValidationErrors().size() );
    }

}
