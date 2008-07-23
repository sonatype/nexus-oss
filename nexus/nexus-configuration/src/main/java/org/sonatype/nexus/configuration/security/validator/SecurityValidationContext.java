package org.sonatype.nexus.configuration.security.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.validator.AbstractValidationContext;


public class SecurityValidationContext extends AbstractValidationContext
{
    private List<String> existingPrivilegeIds;
    
    private List<String> existingRoleIds;
    
    private List<String> existingUserIds;
    
    private Map<String,List<String>> roleContainmentMap;
    
    public void addExistingPrivilegeIds()
    {
        if ( this.existingPrivilegeIds == null )
        {
            this.existingPrivilegeIds = new ArrayList<String>();
        }
    }
    
    public void addExistingRoleIds()
    {
        if ( this.existingRoleIds == null )
        {
            this.existingRoleIds = new ArrayList<String>();
        }
        
        if ( this.roleContainmentMap == null )
        {
            this.roleContainmentMap = new HashMap<String,List<String>>();
        }
    }
    
    public void addExistingUserIds()
    {
        if ( this.existingUserIds == null )
        {
            this.existingUserIds = new ArrayList<String>();
        }
    }
    
    public List<String> getExistingPrivilegeIds()
    {
        return existingPrivilegeIds;
    }
    
    public List<String> getExistingRoleIds()
    {
        return existingRoleIds;
    }
    
    public List<String> getExistingUserIds()
    {
        return existingUserIds;
    }
    
    public Map<String,List<String>> getRoleContainmentMap()
    {
        return roleContainmentMap;
    }
}
