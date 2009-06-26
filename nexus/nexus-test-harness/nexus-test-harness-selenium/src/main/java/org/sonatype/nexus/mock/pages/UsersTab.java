package org.sonatype.nexus.mock.pages;

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

}
