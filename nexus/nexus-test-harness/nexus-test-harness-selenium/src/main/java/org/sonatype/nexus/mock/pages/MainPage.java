package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class MainPage
{
    private Selenium selenium;

    public MainPage( Selenium selenium )
    {
        this.selenium = selenium;
        selenium.open( "/nexus" );
    }

    public boolean loginLinkAvailable()
    {
        String text = selenium.getText( "head-link-r" );

        return "Log In".equals( text );
    }

    public LoginWindow clickLogin()
    {
        if ( loginLinkAvailable() )
        {
            selenium.click( "head-link-r" );
        }
        else
        {
            throw new RuntimeException( "Login link not found!" );
        }

        return new LoginWindow( selenium, this );
    }

    public SchedulesConfigTab openTasks()
    {
        adminPanel().clickScheduleTasks();

        return new SchedulesConfigTab( selenium, this );
    }

    public ViewsPanel viewsPanel()
    {
        return new ViewsPanel( selenium );
    }

    public AdministrationPanel adminPanel()
    {
        return new AdministrationPanel( selenium );
    }

    public SecurityPanel securityPanel()
    {
        return new SecurityPanel( selenium );
    }

    public LogsViewTab openViewLogs()
    {
        viewsPanel().logsAndConfigFilesClick();

        return new LogsViewTab( selenium );
    }

    public RepositoriesTab openRepositories()
    {
        viewsPanel().repositoriesClick();

        return new RepositoriesTab( selenium, this );
    }

    public FeedsTab openFeeds()
    {
        viewsPanel().systemFeedsClick();

        return new FeedsTab( selenium );
    }

    public ServerTab openServer()
    {
        adminPanel().serverClick();

        return new ServerTab( selenium );
    }

    public SearchPanel searchPanel()
    {
        return new SearchPanel( selenium );
    }

    public LogConfigTab openLogsConfig()
    {
        adminPanel().logClick();

        return new LogConfigTab( selenium );
    }

    public UsersTab openUsers()
    {
        securityPanel().usersClick();

        return new UsersTab( selenium );
    }

    public RolesTab openRoles()
    {
        securityPanel().rolesClick();

        return new RolesTab( selenium );
    }

    public PrivilegesTab openPrivileges()
    {
        securityPanel().privilegesClick();

        return new PrivilegesTab( selenium );
    }

    public RepoTargetTab openRepoTargets()
    {
        adminPanel().repositoryTargetsClick();

        return new RepoTargetTab( selenium );
    }

    public RotesTab openRoutes()
    {
        adminPanel().routingClick();

        return new RotesTab( selenium );
    }

}
