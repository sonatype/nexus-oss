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
}
