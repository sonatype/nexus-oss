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
package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class SecurityPanel
    extends SidePanel
{
    public SecurityPanel( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('st-nexus-security')" );
    }

    public boolean changePasswordAvailable()
    {
        return isLinkAvailable( "Change Password" );
    }

    public boolean usersAvailable()
    {
        return isLinkAvailable( "Users" );
    }

    public boolean rolesAvailable()
    {
        return isLinkAvailable( "Roles" );
    }

    public boolean privilegesAvailable()
    {
        return isLinkAvailable( "Privileges" );
    }

    public boolean repositoryTargetsAvailable()
    {
        return isLinkAvailable( "Repository Targets" );
    }

    public ChangePasswordWindow clickChangePassword()
    {
        clickLink( "Change Password" );

        ChangePasswordWindow window = new ChangePasswordWindow( selenium );

        window.waitForVisible();

        return window;
    }

    public void usersClick()
    {
        clickLink( "Users" );
    }

    public void rolesClick()
    {
        clickLink( "Roles" );
    }

    public void privilegesClick()
    {
        clickLink( "Privileges" );
    }

    public boolean ldapConfigurationAvailable()
    {
        return isLinkAvailable( "LDAP Configuration" );
    }

    public void ldapConfigurationClick()
    {
        clickLink( "LDAP Configuration" );
    }
    
    public void repositoryTargetsClick()
    {
        clickLink( "Repository Targets" );
    }
}
