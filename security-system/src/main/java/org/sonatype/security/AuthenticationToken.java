package org.sonatype.security;

public interface AuthenticationToken
{

    public Object getPrincipal();
    
    public Object getCredentials();
    
}
