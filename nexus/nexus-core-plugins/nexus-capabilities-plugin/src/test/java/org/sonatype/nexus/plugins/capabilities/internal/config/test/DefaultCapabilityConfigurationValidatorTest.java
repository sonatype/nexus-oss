package org.sonatype.nexus.plugins.capabilities.internal.config.test;

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
        cap.setName( "UnitTest" );
        cap.setTypeId( "AnyType" );
        CCapabilityProperty prop = new CCapabilityProperty();
        prop.setKey( "key" );
        prop.setValue( "value" );
        cap.addProperty( prop );
        return cap;
    }

    public void testFailValidate()
    {
        CCapability cap = new CCapability();
        CCapabilityProperty prop = new CCapabilityProperty();
        cap.addProperty( prop );

        ValidationResponse res = validator.validate( cap, false );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertEquals( 4, res.getValidationErrors().size() );
    }

    public void testPassValidateModel()
    {
        Configuration cfg = new Configuration();
        cfg.addCapability( createValidCapability() );
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( cfg );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertTrue( res.getValidationErrors().isEmpty() );
    }

    public void testFailValidateModel()
    {
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( null );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertEquals( 1, res.getValidationErrors().size() );
    }

}
