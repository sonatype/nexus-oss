package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.security.authorization.Role;

public interface User
{
    public String getUserId();

    public void setUserId( String userId );

    public String getName();

    public void setName( String name );

    public String getEmailAddress();

    public void setEmailAddress( String emailAddress );

    public String getSource();

    public void setSource( String source );

    public Set<Role> getRoles();

    public void addRole( Role role );

    public void setRoles( Set<Role> roles );
    
    public UserStatus getStatus();
    public void setStatus( UserStatus status );
    
    public boolean isReadOnly();
    public void setReadOnly( boolean readOnly );

}
