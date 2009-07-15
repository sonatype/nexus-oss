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
