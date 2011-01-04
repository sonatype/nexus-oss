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
package org.sonatype.nexus.selenium.nexus2208;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RolesConfigurationForm;
import org.sonatype.nexus.mock.pages.RolesTab;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2208RolesTest.class )
public class Nexus2208RolesTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

        RolesConfigurationForm roles = main.openRoles().addRole();

        NxAssert.requiredField( roles.getRoleId(), "selrole" );
        NxAssert.requiredField( roles.getName(), "selrole" );

        roles.save();
        NxAssert.hasErrorText( roles.getPrivileges(), "One or more roles or privileges are required" );
        roles.getPrivileges().addAll();
        NxAssert.noErrorText( roles.getPrivileges() );

        roles.cancel();
    }

    @Test
    public void roleCRUD()
        throws InterruptedException
    {
        doLogin();

        RolesTab roles = main.openRoles();

        // create
        String roleId = "selrole";
        String name = "selrolename";
        String priv = "admin";
        roles.addRole().populate( roleId, name, priv ).save();
        roles.refresh();

        Assert.assertTrue( roles.getGrid().contains( roleId ) );
        roles.refresh();

        // read
        RolesConfigurationForm role = roles.select( roleId ).selectConfiguration();
        NxAssert.valueEqualsTo( role.getRoleId(), roleId );
        NxAssert.valueEqualsTo( role.getName(), name );
        NxAssert.contains( role.getPrivileges(), priv );

        roles.refresh();

        // update
        String newName = "new selenium role name";

        role = roles.select( roleId ).selectConfiguration();
        role.getName().type( newName );
        role.save();

        roles.refresh();
        role = roles.select( roleId ).selectConfiguration();
        NxAssert.valueEqualsTo( role.getName(), newName );

        roles.refresh();

        // delete
        roles.select( roleId );
        roles.delete().clickYes();
        roles.refresh();

        Assert.assertFalse( roles.getGrid().contains( roleId ) );
    }
}
