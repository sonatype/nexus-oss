package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesEditTabs
    extends Component
{

    public enum RepoKind
    {
        HOSTED( 2 ), PROXY( 2 ), VIRTUAL( 1 );

        private int configPosition;

        private RepoKind( int configPosition )
        {
            this.configPosition = configPosition;
        }
    }

    public RepositoriesEditTabs( Selenium selenium )
    {
        super( selenium, RepositoriesTab.REPOSITORIES_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public RepositoriesConfigurationForm selectConfiguration( RepoKind kind )
    {
        select( kind.configPosition );

        return new RepositoriesConfigurationForm( selenium, expression + ".getLayout().activeItem" );
    }
}
