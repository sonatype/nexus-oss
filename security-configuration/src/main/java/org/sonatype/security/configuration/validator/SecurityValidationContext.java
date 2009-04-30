package org.sonatype.security.configuration.validator;

import org.sonatype.configuration.validation.ValidationContext;
import org.sonatype.security.configuration.model.SecurityConfiguration;

public class SecurityValidationContext
    implements ValidationContext
{

    private SecurityConfiguration securityConfiguration;

    public SecurityConfiguration getSecurityConfiguration()
    {
        return securityConfiguration;
    }

    public void setSecurityConfiguration( SecurityConfiguration securityConfiguration )
    {
        this.securityConfiguration = securityConfiguration;
    }

}
