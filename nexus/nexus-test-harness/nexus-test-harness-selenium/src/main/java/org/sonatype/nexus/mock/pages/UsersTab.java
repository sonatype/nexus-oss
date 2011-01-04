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

import org.sonatype.nexus.mock.components.Menu;

import com.thoughtworks.selenium.Selenium;

public class UsersTab
    extends AbstractTab
{

    public static final String USERS_ST = "window.Ext.getCmp('security-users')";

    public UsersTab( Selenium selenium )
    {
        super( selenium, USERS_ST );
    }

    public UsersConfigurationForm addUser()
    {
        addButton.click();

        addMenu.click( "text", "Nexus User" );

        return new UsersConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public UserEditTabs select( String userId )
    {
        grid.select( userId );

        return new UserEditTabs( selenium );
    }

    public MessageBox contextMenuResetPassword( String userId )
    {
        Menu menu = grid.openContextMenu( userId, 1, "grid-context-menu" );
        menu.click( "text", "Reset Password" );

        return new MessageBox( selenium );
    }

    public SetPasswordWindow contextMenuSetPassword( String userId )
    {
        Menu menu = grid.openContextMenu( userId, 1, "grid-context-menu" );
        menu.click( "text", "Set Password" );

        return new SetPasswordWindow( selenium );
    }

}
