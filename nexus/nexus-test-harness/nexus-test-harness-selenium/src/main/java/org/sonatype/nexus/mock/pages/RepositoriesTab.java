package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.NexusTestCase;
import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesTab
    extends Component
{

    public static final String REPOSITORIES_ST = "window.Ext.getCmp('view-repositories')";

    private MainPage mainPage;

    private RepositoriesGrid repositoriesGrid;

    private Button refreshButton;

    private Button addButton;

    private Button deleteButton;

    private Button typeButton;

    private Button addHostedButton;

    private Button addVirtualButton;

    private Button addProxyButton;

    private Button addGroupButton;

    public RepositoriesTab( Selenium selenium, MainPage mainPage )
    {
        super( selenium, REPOSITORIES_ST );
        this.mainPage = mainPage;

        this.repositoriesGrid = new RepositoriesGrid( selenium );

        this.refreshButton = new Button( selenium, expression + ".refreshButton" );
        this.deleteButton = new Button( selenium, expression + ".toolbarDeleteButton" );
        this.typeButton = new Button( selenium, expression + ".browseTypeButton" );

        this.addButton = new Button( selenium, expression + ".toolbarAddButton" );
        this.addHostedButton = new Button( selenium, addButton.getExpression() + ".menu.items.items[0].el" );
        this.addHostedButton.idFunction = ".id";
        this.addProxyButton = new Button( selenium, addButton.getExpression() + ".menu.items.items[1].el" );
        this.addProxyButton.idFunction = ".id";
        this.addVirtualButton = new Button( selenium, addButton.getExpression() + ".menu.items.items[2].el" );
        this.addVirtualButton.idFunction = ".id";
        this.addGroupButton = new Button( selenium, addButton.getExpression() + ".menu.items.items[4].el" );
        this.addGroupButton.idFunction = ".id";
    }

    public RepositoriesConfigurationForm addHostedRepo()
    {
        addButton.click();

        addHostedButton.clickNoWait();

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesConfigurationForm addProxyRepo()
    {
        addButton.click();

        addProxyButton.clickNoWait();

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesConfigurationForm addVirtualRepo()
    {
        addButton.click();

        addVirtualButton.clickNoWait();

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesTab refresh()
    {
        refreshButton.click();

        return this;
    }

    public RepositoriesEditTabs select( String repoId, RepoKind kind )
    {
        this.repositoriesGrid.waitToLoad();

        if ( RepoKind.GROUP.equals( kind ) )
        {
            this.repositoriesGrid.select( NexusTestCase.nexusBaseURL + "service/local/repo_groups/" + repoId );
        }
        else
        {
            this.repositoriesGrid.select( NexusTestCase.nexusBaseURL + "service/local/repositories/" + repoId );
        }

        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            //
        }

        return new RepositoriesEditTabs( selenium );
    }

    public MainPage getMainPage()
    {
        return mainPage;
    }

    public RepositoriesGrid getRepositoriesGrid()
    {
        return repositoriesGrid;
    }

    public Button getRefreshButton()
    {
        return refreshButton;
    }

    public Button getAddButton()
    {
        return addButton;
    }

    public Button getDeleteButton()
    {
        return deleteButton;
    }

    public Button getTypeButton()
    {
        return typeButton;
    }

    public Button getAddHostedButton()
    {
        return addHostedButton;
    }

    public MessageBox delete()
    {
        this.deleteButton.click();

        return new MessageBox( selenium );
    }

    public boolean contains( String repoId )
    {
        return this.repositoriesGrid.contains( NexusTestCase.nexusBaseURL + "/service/local/repositories/" + repoId );
    }

    public GroupConfigurationForm addGroup()
    {
        addButton.click();

        addGroupButton.clickNoWait();

        return new GroupConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

}
