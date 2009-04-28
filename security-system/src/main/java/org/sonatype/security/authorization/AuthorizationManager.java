package org.sonatype.security.authorization;

import java.util.Set;

public interface AuthorizationManager
{
    public String getSource();
    
    public Set<Role> listRoles();
    
    //FIXME: I have a feeling Permissions will need to be more then just strings
    public Set<String> listPermissions();
    
}
