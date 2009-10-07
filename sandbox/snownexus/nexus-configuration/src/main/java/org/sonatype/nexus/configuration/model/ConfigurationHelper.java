package org.sonatype.nexus.configuration.model;

public interface ConfigurationHelper
{
    Configuration clone( Configuration config );
    void encryptDecryptPasswords( Configuration config, boolean encrypt );
    void maskPasswords( Configuration config );
}
