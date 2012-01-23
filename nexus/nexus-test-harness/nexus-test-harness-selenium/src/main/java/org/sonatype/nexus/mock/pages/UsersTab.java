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
