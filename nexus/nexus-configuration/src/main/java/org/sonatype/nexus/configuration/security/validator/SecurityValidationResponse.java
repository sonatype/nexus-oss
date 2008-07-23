package org.sonatype.nexus.configuration.security.validator;

import org.sonatype.nexus.configuration.validator.AbstractValidationResponse;
import org.sonatype.nexus.configuration.validator.ValidationContext;

public class SecurityValidationResponse 
    extends AbstractValidationResponse
{
    @Override
    protected ValidationContext doGetContext()
    {
        return new SecurityValidationContext();
    }
}
