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
package org.sonatype.security.realms.tools;

import java.util.List;

import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;

public class DefaultSecurityConfigurationCleanerTest
    extends AbstractSecurityTestCase
{
    private DefaultSecurityConfigurationCleaner cleaner;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        cleaner = (DefaultSecurityConfigurationCleaner) lookup( SecurityConfigurationCleaner.class );
    }

    public void testRemovePrivilege()
        throws Exception
    {
        Configuration configuration =
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/security/realms/tools/cleaner-security.xml" ) );

        CPrivilege priv = (CPrivilege) configuration.getPrivileges().get( 0 );

        configuration.removePrivilege( priv );

        cleaner.privilegeRemoved( new EnhancedConfiguration( configuration ), priv.getId() );

        for ( CRole role : (List<CRole>) configuration.getRoles() )
        {
            assertFalse( role.getPrivileges().contains( priv.getId() ) );
        }
    }

    public void testRemoveRole()
        throws Exception
    {
        Configuration configuration =
            getConfigurationFromStream( getClass().getResourceAsStream(
                "/org/sonatype/security/realms/tools/cleaner-security.xml" ) );

        CRole role = (CRole) configuration.getRoles().get( 0 );

        configuration.removeRole( role );

        cleaner.roleRemoved( new EnhancedConfiguration( configuration ), role.getId() );

        for ( CRole crole : (List<CRole>) configuration.getRoles() )
        {
            assertFalse( crole.getPrivileges().contains( role.getId() ) );
        }

        for ( CUserRoleMapping mapping : (List<CUserRoleMapping>) configuration.getUserRoleMappings() )
        {
            assertFalse( mapping.getRoles().contains( role.getId() ) );
        }
    }
}
