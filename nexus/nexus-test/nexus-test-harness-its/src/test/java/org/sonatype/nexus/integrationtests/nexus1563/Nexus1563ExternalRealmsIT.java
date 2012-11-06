/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
