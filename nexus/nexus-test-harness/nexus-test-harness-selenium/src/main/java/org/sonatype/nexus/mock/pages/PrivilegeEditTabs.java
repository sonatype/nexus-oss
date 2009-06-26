package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class PrivilegeEditTabs
extends Component
{

    public PrivilegeEditTabs( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public PrivilegeConfigurationForm selectConfiguration()
    {
        select( 0 );

        return new PrivilegeConfigurationForm( selenium, expression + ".getLayout().activeItem" );
    }

}
