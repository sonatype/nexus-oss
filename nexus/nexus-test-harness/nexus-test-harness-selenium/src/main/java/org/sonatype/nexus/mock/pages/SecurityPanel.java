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
