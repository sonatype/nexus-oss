package org.sonatype.nexus.plugins.capabilities.internal.config;

import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.Configuration;

@Singleton
public class DefaultCapabilityConfigurationValidator
    implements CapabilityConfigurationValidator
{

    public ValidationResponse validate( final CCapability capability, final boolean isCreateMode )
    {
        final ValidationResponse response = new ValidationResponse();

        if ( !isCreateMode && StringUtils.isEmpty( capability.getId() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "id", "Capability ID cannot be empty." );

            response.addValidationError( msg );
        }
        if ( StringUtils.isEmpty( capability.getName() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "name", "Capability name cannot be empty." );

            response.addValidationError( msg );
        }
        if ( StringUtils.isEmpty( capability.getTypeId() ) )
        {
            final ValidationMessage msg = new ValidationMessage( "typeId", "Capability type cannot be empty." );

            response.addValidationError( msg );
        }

        for ( final CCapabilityProperty property : capability.getProperties() )
        {
            if ( StringUtils.isEmpty( property.getKey() ) )
            {
                final ValidationMessage msg =
                    new ValidationMessage( "type", "Capability properties cannot cannot have an empty key." );

                response.addValidationError( msg );
                break;
            }
        }

        return response;
    }

    @SuppressWarnings( "unchecked" )
    public ValidationResponse validateModel( final ValidationRequest request )
    {
        final ValidationResponse response = new ValidationResponse();

        final Configuration configuration = (Configuration) request.getConfiguration();

        if ( configuration == null )
        {
            final ValidationMessage msg = new ValidationMessage( "*", "No configuration available to validate" );

            response.addValidationError( msg );
        }
        else
        {
            for ( final CCapability capability : configuration.getCapabilities() )
            {
                response.append( validate( capability, false ) );
            }
        }

        return response;
    }
}
