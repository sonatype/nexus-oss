package org.sonatype.security.model.upgrade;

import java.util.List;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.security.model.v2_0_2.CUser;
import org.sonatype.security.model.v2_0_2.CUserRoleMapping;
import org.sonatype.security.model.v2_0_2.Configuration;

public class MockSecurityDataUpgrader
    extends AbstractDataUpgrader<Configuration>
    implements SecurityDataUpgrader
{

    @Override
    public void doUpgrade( Configuration configuration )
        throws ConfigurationIsCorruptedException
    {
        // replace the admin user's name with admin-user
        for ( CUser user : (List<CUser>) configuration.getUsers() )
        {
            if ( user.getId().equals( "admin" ) )
            {
                user.setId( "admin-user" );
            }
        }
        
        for ( CUserRoleMapping roleMapping : (List<CUserRoleMapping>) configuration.getUserRoleMappings() )
        {
            if ( roleMapping.getUserId().equals( "admin" ) )
            {
                roleMapping.setUserId( "admin-user" );
            }
        }
        
    }

}
