package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.sonatype.jsecurity.AbstractSecurityTestCase;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.model.CUserRoleMapping;
import org.sonatype.jsecurity.model.Configuration;

public class DefaultSecurityConfigurationCleanerTest
    extends AbstractSecurityTestCase
{
    private DefaultSecurityConfigurationCleaner cleaner;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        cleaner = ( DefaultSecurityConfigurationCleaner ) lookup( SecurityConfigurationCleaner.class );
    }
    
    public void testRemovePrivilege()
        throws Exception
    {
        Configuration configuration = getConfigurationFromStream( getClass().getResourceAsStream(
        "/org/sonatype/jsecurity/realms/tools/cleaner-security.xml" ) );
        
        CPrivilege priv = ( CPrivilege ) configuration.getPrivileges().get(0);
        
        configuration.removePrivilege( priv );
        
        cleaner.privilegeRemoved( configuration, priv.getId() );
        
        for ( CRole role : ( List<CRole> ) configuration.getRoles() )
        {
            assertFalse( role.getPrivileges().contains( priv.getId() ) );
        }
    }
    
    public void testRemoveRole()
        throws Exception
    {
        Configuration configuration = getConfigurationFromStream( getClass().getResourceAsStream(
        "/org/sonatype/jsecurity/realms/tools/cleaner-security.xml" ) );
        
        CRole role = ( CRole ) configuration.getRoles().get( 0 );
        
        configuration.removeRole( role );
        
        cleaner.roleRemoved( configuration, role.getId() );
        
        for ( CRole crole : ( List<CRole> ) configuration.getRoles() )
        {
            assertFalse( crole.getPrivileges().contains( role.getId() ) );
        }
        
        for ( CUserRoleMapping mapping : ( List<CUserRoleMapping> ) configuration.getUserRoleMappings() )
        {
            assertFalse( mapping.getRoles().contains( role.getId() ) );
        }
    }
}
