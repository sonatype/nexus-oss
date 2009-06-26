package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RoleEditTabs
    extends Component
{

    public RoleEditTabs( Selenium selenium )
    {
        super( selenium, RolesTab.ROLES_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public RolesConfigurationForm selectConfiguration()
    {
        select( 0 );

        return new RolesConfigurationForm( selenium, expression + ".getLayout().activeItem" );
    }

}
