package org.sonatype.security.configuration.validator;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;

@Component( role = SecurityConfigurationValidator.class )
public class DefaultSecurityConfigurationValidator
    implements SecurityConfigurationValidator
{

    public ValidationResponse<SecurityValidationContext> validateModel( SecurityValidationContext context,
        ValidationRequest<SecurityConfiguration> request )
    {
        ValidationResponse<SecurityValidationContext> validationResponse = new ValidationResponse<SecurityValidationContext>();
        validationResponse.setContext( context );

        SecurityConfiguration configuration = request.getConfiguration();

        validationResponse.append( this.validateAnonymousUsername( context, configuration.getAnonymousUsername() ) );
        validationResponse.append( this.validateAnonymousPassword( context, configuration.getAnonymousPassword() ) );
        validationResponse.append( this.validateRealms( context, configuration.getRealms() ) );

        return validationResponse;
    }

    public ValidationResponse<SecurityValidationContext> validateAnonymousPassword( SecurityValidationContext context,
        String anonymousPassword )
    {
        // we are not currently doing anything here
        ValidationResponse<SecurityValidationContext> validationResponse = new ValidationResponse<SecurityValidationContext>();
        validationResponse.setContext( context );
        return validationResponse;
    }

    public ValidationResponse<SecurityValidationContext> validateAnonymousUsername( SecurityValidationContext context,
        String anonymousUsername )
    {
        // we are not currently doing anything here
        ValidationResponse<SecurityValidationContext> validationResponse = new ValidationResponse<SecurityValidationContext>();
        validationResponse.setContext( context );
        return validationResponse;
    }

    public ValidationResponse<SecurityValidationContext> validateRealms( SecurityValidationContext context,
        List<String> realms )
    {
        ValidationResponse<SecurityValidationContext> validationResponse = new ValidationResponse<SecurityValidationContext>();
        validationResponse.setContext( context );

        if ( ( context.getSecurityConfiguration() != null && context.getSecurityConfiguration().isEnabled() )
            || context.getSecurityConfiguration() == null )
        {
            if ( realms.size() < 1 )
            {
                validationResponse
                    .addValidationError( "Security is enabled, You must have at least one realm enabled." );
            }
            // TODO: we should also try to load each one to see if it exists
        }

        return validationResponse;
    }

}
