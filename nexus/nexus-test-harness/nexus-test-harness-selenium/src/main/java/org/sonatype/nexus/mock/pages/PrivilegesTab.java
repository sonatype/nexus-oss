package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class PrivilegesTab
    extends AbstractTab
{

    public static final String PRIVS_ST = "window.Ext.getCmp('security-privileges')";

    public PrivilegesTab( Selenium selenium )
    {
        super( selenium, PRIVS_ST );
    }

    public PrivilegeConfigurationForm addPrivilege()
    {
        addButton.click();

        addMenu.click( "text", "Repository Target Privilege" );

        return new PrivilegeConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public PrivilegeConfigurationForm select( String privId )
    {
        grid.select( privId );

        return new PrivilegeConfigurationForm( selenium, expression + ".cardPanel.getLayout().activeItem" );
        //return new PrivilegeEditTabs( selenium, expression + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

}
