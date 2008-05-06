package org.sonatype.nexus.security;

import java.io.IOException;

public interface MutableAuthenticationSource
    extends AuthenticationSource
{
    public void setPassword( String username, String secret )
        throws IOException;

    public void unsetPassword( String username )
        throws IOException;
}
