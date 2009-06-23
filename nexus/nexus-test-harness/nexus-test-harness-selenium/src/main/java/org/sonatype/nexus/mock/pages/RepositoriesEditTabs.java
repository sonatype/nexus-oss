package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesEditTabs
    extends Component
{

    public enum RepoKind
    {
        HOSTED( 2 ), PROXY( 2 ), VIRTUAL( 1 ), GROUP( 2 );

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

    public Component selectConfiguration( RepoKind kind )
    {
        select( kind.configPosition );

        if ( RepoKind.GROUP.equals( kind ) )
        {
            return new GroupConfigurationForm( selenium, expression + ".getLayout().activeItem" );
        }
        else
        {
            return new RepositoriesConfigurationForm( selenium, expression + ".getLayout().activeItem" );
        }
    }

    public RepositoriesArtifactUploadForm selectUpload( RepoKind kind )
    {
        if ( !RepoKind.HOSTED.equals( kind ) )
        {
            return null;
        }

        select( 5 );

        return new RepositoriesArtifactUploadForm( selenium, expression + ".getLayout().activeItem" );
    }
}
