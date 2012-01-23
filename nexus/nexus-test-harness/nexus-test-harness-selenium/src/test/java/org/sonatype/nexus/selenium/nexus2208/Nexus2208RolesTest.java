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
