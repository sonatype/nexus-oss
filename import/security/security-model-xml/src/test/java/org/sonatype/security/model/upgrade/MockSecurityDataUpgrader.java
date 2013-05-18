/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
