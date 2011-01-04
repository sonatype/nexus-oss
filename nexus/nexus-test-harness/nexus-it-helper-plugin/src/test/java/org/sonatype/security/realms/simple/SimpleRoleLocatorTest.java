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
package org.sonatype.security.realms.simple;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.Role;

public class SimpleRoleLocatorTest
    extends PlexusTestCase
{

    public void testListRoleIds()
        throws Exception
    {
        AuthorizationManager roleLocator = this.lookup( AuthorizationManager.class, "Simple" );

        Set<String> roleIds = this.toIdSet( roleLocator.listRoles() );
        Assert.assertTrue( roleIds.contains( "role-xyz" ) );
        Assert.assertTrue( roleIds.contains( "role-abc" ) );
        Assert.assertTrue( roleIds.contains( "role-123" ) );
    }

    private Set<String> toIdSet( Set<Role> roles )
    {
        Set<String> ids = new HashSet<String>();

        for ( Role role : roles )
        {
            ids.add( role.getRoleId() );
        }

        return ids;
    }

}
