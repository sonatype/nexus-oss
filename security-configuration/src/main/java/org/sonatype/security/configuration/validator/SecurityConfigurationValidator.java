package org.sonatype.security.configuration.validator;

import java.util.List;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;

public interface SecurityConfigurationValidator
{
    ValidationResponse<SecurityValidationContext> validateModel( SecurityValidationContext context,
        ValidationRequest<SecurityConfiguration> request );

    ValidationResponse<SecurityValidationContext> validateAnonymousUsername( SecurityValidationContext context,
        String anonymousUsername );

    ValidationResponse<SecurityValidationContext> validateAnonymousPassword( SecurityValidationContext context,
        String anonymousPassword );

    ValidationResponse<SecurityValidationContext> validateRealms( SecurityValidationContext context, List<String> realms );
}
