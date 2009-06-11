package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesEditTabs
    extends Component
{

    public RepositoriesEditTabs( Selenium selenium )
    {
        super( selenium, RepositoriesTab.REPOSITORIES_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public RepositoriesConfigurationForm selectConfiguration()
    {
        select( 2 );

        return new RepositoriesConfigurationForm( selenium, expression + ".getLayout().activeItem" );
    }
}
