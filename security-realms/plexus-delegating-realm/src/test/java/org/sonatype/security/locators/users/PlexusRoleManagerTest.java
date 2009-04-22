/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.locators.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusRoleManager;

public class PlexusRoleManagerTest
    extends PlexusTestCase
{

    private PlexusRoleManager getRoleManager()
        throws Exception
    {
        return (PlexusRoleManager) this.lookup( PlexusRoleManager.class );
    }

    public void testGetAll()
        throws Exception
    {
        PlexusRoleManager roleManager = this.getRoleManager();
        Set<PlexusRole> roles = roleManager.listRoles( PlexusRoleManager.SOURCE_ALL );
        Assert.assertFalse( roles.isEmpty() );

        Map<String, PlexusRole> roleMap = this.getMapFromList( roles );

        Assert.assertTrue( roleMap.containsKey( "role123" ) );
        Assert.assertTrue( roleMap.containsKey( "role124" ) );
        Assert.assertTrue( roleMap.containsKey( "role125" ) );
        Assert.assertTrue( roleMap.containsKey( "role126" ) );

        Assert.assertTrue( roleMap.containsKey( "role23" ) );
        Assert.assertTrue( roleMap.containsKey( "role24" ) );
        Assert.assertTrue( roleMap.containsKey( "role25" ) );
        Assert.assertTrue( roleMap.containsKey( "role26" ) );       
        Assert.assertEquals( 10, roles.size() );
    }

    private Map<String, PlexusRole> getMapFromList( Set<PlexusRole> roles )
    {
        Map<String, PlexusRole> roleMap = new HashMap<String, PlexusRole>();
        for ( PlexusRole role : roles )
        {
            roleMap.put( role.getRoleId(), role );
        }
        return roleMap;
    }

}
