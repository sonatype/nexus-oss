package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.security.authorization.Role;

public interface User
{
    public String getUserId();
    
    public String getName();
    
    public String getEmailAddress();

    public String getSource();
    
    public Set<Role> getRoles();
    
}
