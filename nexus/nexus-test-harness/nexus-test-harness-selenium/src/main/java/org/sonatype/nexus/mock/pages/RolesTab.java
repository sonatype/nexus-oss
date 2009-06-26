package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class RolesTab
    extends AbstractTab
{

    public static final String ROLES_ST = "window.Ext.getCmp('security-roles')";

    public RolesTab( Selenium selenium )
    {
        super( selenium, ROLES_ST );
    }

    public RolesConfigurationForm addRole()
    {
        addButton.click();

        addMenu.click( "text", "Nexus Role" );

        return new RolesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RoleEditTabs select( String roleId )
    {
        grid.select( roleId );

        return new RoleEditTabs( selenium );
    }
}
