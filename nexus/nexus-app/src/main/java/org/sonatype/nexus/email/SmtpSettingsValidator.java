package org.sonatype.nexus.email;

import org.sonatype.nexus.configuration.model.CSmtpConfiguration;

public interface SmtpSettingsValidator
{
    boolean sendSmtpConfigurationTest( CSmtpConfiguration config, String email )
        throws EmailerException;
}
