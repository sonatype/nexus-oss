package org.sonatype.jsecurity.locators;

import java.util.Set;

public interface RoleResolver
{

    /**
     * Resolves the nested Roles contained in other roles.
     * 
     * If Role A contains Z, and Role B contains X. then:<BR/>
     * <code>resolveRoles( new List( A,B ) )</code> would return <code>A, B, X, Z</code>.
     * 
     * @param roleIds
     * @return
     */
    public Set<String> resolveRoles( Set<String> roleIds);
    
    /**
     * Resolves all permissions contained Roles and nested Roles of <code>roleIds</code>.
     * 
     * @param roleIds
     * @return
     */
    public Set<String> resolvePermissions( Set<String> roleIds);
    
    /**
     * Retrieves all roles that contain this role (including itself)
     * 
     * If Role A contains Z, and Role B contains Z.  then: <BR/>
     * <code>effectiveRoles( new List( Z ) )</code> would return <code>A, B, Z</code>
     * 
     * @param roleIds
     * @return
     */
    public Set<String> effectiveRoles( Set<String> roleIds );
    
}
