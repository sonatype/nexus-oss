package org.sonatype.nexus.security.filter.authc;

public interface PasswordDecryptor
{
    boolean isEncryptedPassword( String text );

    String getDecryptedPassword( String text );
}
