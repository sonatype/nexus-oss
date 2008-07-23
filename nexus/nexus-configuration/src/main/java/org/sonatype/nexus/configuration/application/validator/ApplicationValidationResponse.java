package org.sonatype.nexus.configuration.application.validator;

import org.sonatype.nexus.configuration.validator.AbstractValidationResponse;
import org.sonatype.nexus.configuration.validator.ValidationContext;

public class ApplicationValidationResponse
    extends AbstractValidationResponse
{
    @Override
    protected ValidationContext doGetContext()
    {
        return new ApplicationValidationContext();
    }
}
