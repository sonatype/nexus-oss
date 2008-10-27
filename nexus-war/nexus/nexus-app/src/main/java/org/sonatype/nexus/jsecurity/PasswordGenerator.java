package org.sonatype.nexus.jsecurity;

public interface PasswordGenerator
{
    String generatePassword( int minChars, int maxChars );

    String hashPassword( String password );
}
