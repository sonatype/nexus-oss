package org.sonatype.security.usermanagement;

import java.util.List;
import java.util.Map;
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
    
    public void addRole( RoleIdentifier roleIdentifier );
    
    public void addAllRoles( Set<RoleIdentifier> roleIdentifiers );
    
    public Set<RoleIdentifier> getRoles();
    
    public void setRoles( Set<RoleIdentifier> roles );
    
    public UserStatus getStatus();
    public void setStatus( UserStatus status );
    
    // TODO: will be removed
    public boolean isReadOnly();
    public void setReadOnly( boolean readOnly );

}
