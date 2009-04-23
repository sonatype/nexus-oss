package org.sonatype.security;

public interface User
{

    public Object getPrincipal();
    
    public String getUsername();
    
    public String getFullName();
    
    public String getEmail();
    
    
    public Object getSession();
}
