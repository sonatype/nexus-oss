package org.sonatype.nexus.plugins.capabilities.internal.config.test;

import org.codehaus.plexus.ContainerConfiguration;
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

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );

        configuration.setClassPathScanning( true );
    }

    private CapabilityConfigurationValidator validator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // TODO uncoment
        // validator = lookup( CapabilityConfigurationValidator.class );
    }

    public void testValidate()
    {
        if ( validator == null )
        {
            // TODO remove this
            return;
        }
        CCapability cap = new CCapability();
        cap.setName( "UnitTest" );
        cap.setTypeId( "AnyType" );
        CCapabilityProperty prop = new CCapabilityProperty();
        prop.setKey( "key" );
        prop.setValue( "value" );
        cap.addProperty( prop );

        ValidationResponse res = validator.validate( cap, true );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertTrue( res.getValidationErrors().isEmpty() );
    }

    public void testPassValidateModel()
    {
        if ( validator == null )
        {
            // TODO remove this
            return;
        }
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( new Configuration() );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertTrue( res.getValidationErrors().isEmpty() );
    }

    public void testFailValidateModel()
    {
        if ( validator == null )
        {
            // TODO remove this
            return;
        }
        ValidationRequest<Configuration> req = new ValidationRequest<Configuration>( null );
        ValidationResponse res = validator.validateModel( req );
        assertTrue( res.getValidationWarnings().isEmpty() );
        assertEquals( 1, res.getValidationErrors().size() );
    }

}
