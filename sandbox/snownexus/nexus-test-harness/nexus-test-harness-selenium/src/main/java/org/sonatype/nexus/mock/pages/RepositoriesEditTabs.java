package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesEditTabs
    extends Component
{

    public enum RepoKind
    {
        HOSTED( 2, 4, 3 ), PROXY( 2, 4, 3 ), VIRTUAL( 1, 2, -1 ), GROUP( 2, -1, -1 );

        private int configPosition;

        private int summaryPosition;

        private int mirrorPosition;

        private RepoKind( int configPosition, int summaryPosition, int mirrorPosition )
        {
            this.configPosition = configPosition;
            this.summaryPosition = summaryPosition;
            this.mirrorPosition = mirrorPosition;
        }
    }

    private RepoKind kind;

    public RepositoriesEditTabs( Selenium selenium, RepoKind kind )
    {
        super( selenium, RepositoriesTab.REPOSITORIES_ST + ".cardPanel.getLayout().activeItem.tabPanel" );
        this.kind = kind;
    }

    public void select( int i )
    {
        runScript( ".activate(" + expression + ".items.items[" + i + "])" );
    }

    public Component selectConfiguration()
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

    public RepositoriesArtifactUploadForm selectUpload()
    {
        if ( !RepoKind.HOSTED.equals( kind ) )
        {
            return null;
        }

        select( 5 );

        return new RepositoriesArtifactUploadForm( selenium, expression + ".getLayout().activeItem" );
    }

    public RepositorySummary selectSummary()
    {
        if ( RepoKind.GROUP.equals( kind ) )
        {
            return null;
        }

        select( kind.summaryPosition );

        return new RepositorySummary( selenium, expression + ".getLayout().activeItem" );
    }

    public RepositoryMirror selectMirror()
    {
        if ( !( RepoKind.PROXY.equals( kind ) || RepoKind.HOSTED.equals( kind ) ) )
        {
            return null;
        }

        select( kind.mirrorPosition );

        return new RepositoryMirror( selenium, expression + ".getLayout().activeItem" );
    }
}
