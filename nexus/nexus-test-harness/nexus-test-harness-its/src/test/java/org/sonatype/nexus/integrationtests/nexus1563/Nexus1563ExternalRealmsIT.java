/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1563;

import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1563ExternalRealmsIT
    extends AbstractNexusIntegrationTest
{

    private UserMessageUtil userUtil;

    private RoleMessageUtil roleUtil;

    @BeforeMethod
    public void init()
    {
        this.userUtil = new UserMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void getExternalUsers()
        throws Exception
    {
        List<PlexusUserResource> users = userUtil.searchPlexusUsers( "Simple" );
        Assert.assertTrue( containsUser( users, "admin-simple" ), "User not found" );
        Assert.assertTrue( containsUser( users, "anonymous-simple" ), "User not found" );
        Assert.assertTrue( containsUser( users, "deployment-simple" ), "User not found" );
    }

    @Test
    public void getExternalRoles()
        throws Exception
    {
        List<PlexusRoleResource> roles = roleUtil.getRoles( "Simple" );
        Assert.assertTrue( containsRole( roles, "role-123" ), "Role not found" );
        Assert.assertTrue( containsRole( roles, "role-abc" ), "Role not found" );
        Assert.assertTrue( containsRole( roles, "role-xyz" ), "Role not found" );
    }

    @Test
    public void searchUser()
        throws Exception
    {
        List<PlexusUserResource> users = userUtil.searchPlexusUsers( "Simple", "admin-simple" );
        Assert.assertEquals( users.size(), 1, "User not found" );
    }

    private boolean containsRole( List<PlexusRoleResource> roles, String roleName )
    {
        for ( PlexusRoleResource role : roles )
        {
            if ( roleName.equals( role.getRoleId() ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean containsUser( List<PlexusUserResource> users, String userId )
    {
        for ( PlexusUserResource user : users )
        {
            if ( userId.equals( user.getUserId() ) )
            {
                return true;
            }
        }
        return false;
    }
}
