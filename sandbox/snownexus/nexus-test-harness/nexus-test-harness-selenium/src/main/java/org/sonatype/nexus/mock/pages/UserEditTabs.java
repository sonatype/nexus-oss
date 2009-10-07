package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class UserEditTabs
    extends Component
{

    public UserEditTabs( Selenium selenium )
    {
        super( selenium, UsersTab.USERS_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public UsersConfigurationForm selectConfiguration()
    {
        select( 0 );

        return new UsersConfigurationForm( selenium, expression + ".getLayout().activeItem" );
    }

}
