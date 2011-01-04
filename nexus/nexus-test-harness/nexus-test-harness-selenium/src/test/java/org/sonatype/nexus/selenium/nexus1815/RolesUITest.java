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
package org.sonatype.nexus.selenium.nexus1815;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.AdministrationPanel;
import org.sonatype.nexus.mock.pages.SecurityPanel;
import org.sonatype.nexus.mock.pages.ViewsPanel;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Component( role = RolesUITest.class )
public class RolesUITest
    extends SeleniumTest
{

    private static final String USER_PW = "password";

    @Requirement
    private SecuritySystem securitySystem;

    private DefaultUser user;

    @BeforeClass
    public void createUser()
        throws Exception
    {
        user = new DefaultUser();
        user.setUserId( "role-admin" );
        user.setName( "role-admin" );
        user.setEmailAddress( "email@sonatype.org" );

        Set<RoleIdentifier> roles = new LinkedHashSet<RoleIdentifier>();
        roles.add( new RoleIdentifier( "ui-basic", "ui-basic" ) );
        roles.add( new RoleIdentifier( "ui-roles-admin", "ui-roles-admin" ) );
        user.setRoles( roles );
        user.setStatus( UserStatus.active );
        user.setSource( "default" );
        securitySystem.addUser( user, USER_PW );
    }

    @AfterClass
    public void deleteUser()
        throws UserNotFoundException
    {
        securitySystem.deleteUser( user.getUserId() );
    }

    @Test
    public void admin()
    {
        main.clickLogin().populate( User.ADMIN ).loginExpectingSuccess();

        // views/repositories
        ViewsPanel viewsPanel = main.viewsPanel();
        viewsPanel.expand();
        assertTrue( "Repositories link should be visible", viewsPanel.repositoriesAvailable() );
        assertTrue( "System Feeds link should be visible", viewsPanel.systemFeedsAvailable() );

        // now collapse the panel and check
        viewsPanel.collapse();
        assertFalse( "Repositories link should not be visible", viewsPanel.repositoriesAvailable() );
        assertFalse( "System Feeds link should not be visible", viewsPanel.systemFeedsAvailable() );

        // administration
        AdministrationPanel adminPanel = main.adminPanel();
        adminPanel.expand();
        assertTrue( "Logs and Config Files link should be visible", adminPanel.logsAndConfigFilesAvailable() );
        assertTrue( "Server link should be visible", adminPanel.serverAvailable() );
        assertTrue( "Routing link should be visible", adminPanel.routingAvailable() );
        assertTrue( "Scheduled Tasks link should be visible", adminPanel.scheduleTasksAvailable() );
        assertTrue( "Log link should be visible", adminPanel.logAvailable() );

        // now collapse the panel and check
        adminPanel.collapse();
        assertFalse( "Logs and Config Files link should not be visible", adminPanel.logsAndConfigFilesAvailable() );
        assertFalse( "Server link should not be visible", adminPanel.serverAvailable() );
        assertFalse( "Routing link should not be visible", adminPanel.routingAvailable() );
        assertFalse( "Scheduled Tasks link should not be visible", adminPanel.scheduleTasksAvailable() );
        assertFalse( "Log link should not be visible", adminPanel.logAvailable() );

        // security
        SecurityPanel securityPanel = main.securityPanel();
        securityPanel.expand();
        assertTrue( "Change Password link should be visible", securityPanel.changePasswordAvailable() );
        assertTrue( "Users link should be visible", securityPanel.usersAvailable() );
        assertTrue( "Roles link should be visible", securityPanel.rolesAvailable() );
        assertTrue( "Privileges link should be visible", securityPanel.privilegesAvailable() );
        assertTrue( "Repository Targets link should be visible", securityPanel.repositoryTargetsAvailable() );

        // now collapse and check again
        securityPanel.collapse();
        assertFalse( "Change Password link should not be visible", securityPanel.changePasswordAvailable() );
        assertFalse( "Users link should not be visible", securityPanel.usersAvailable() );
        assertFalse( "Roles link should not be visible", securityPanel.rolesAvailable() );
        assertFalse( "Privileges link should not be visible", securityPanel.privilegesAvailable() );
        assertFalse( "Repository Targets link should not be visible", securityPanel.repositoryTargetsAvailable() );
    }

    @Test
    public void roleAdmin()
    {
        main.clickLogin().populate( user.getUserId(), USER_PW ).loginExpectingSuccess();

        // views/repositories
        ViewsPanel viewsPanel = main.viewsPanel();
        assertTrue( "Views panel should not be disaplyed at all", viewsPanel.hidden() );

        // administration
        AdministrationPanel adminPanel = main.adminPanel();
        assertTrue( "Admin panel should not be disaplyed at all", adminPanel.hidden() );

        // security
        SecurityPanel securityPanel = main.securityPanel();
        securityPanel.expand();
        assertTrue( "Change Password link should be visible", securityPanel.changePasswordAvailable() );
        assertFalse( "Users link should not be visible", securityPanel.usersAvailable() );
        assertTrue( "Roles link should be visible", securityPanel.rolesAvailable() );
        assertFalse( "Privileges link should not be visible", securityPanel.privilegesAvailable() );
    }
}
