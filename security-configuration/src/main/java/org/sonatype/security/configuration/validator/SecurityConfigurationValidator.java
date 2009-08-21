package org.sonatype.security.configuration.validator;

import java.util.List;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;

public interface SecurityConfigurationValidator
{
    ValidationResponse validateModel( SecurityValidationContext context,
                                      ValidationRequest<SecurityConfiguration> request );

    ValidationResponse validateAnonymousUsername( SecurityValidationContext context, String anonymousUsername );

    ValidationResponse validateAnonymousPassword( SecurityValidationContext context, String anonymousPassword );

    ValidationResponse validateRealms( SecurityValidationContext context, List<String> realms );
}
