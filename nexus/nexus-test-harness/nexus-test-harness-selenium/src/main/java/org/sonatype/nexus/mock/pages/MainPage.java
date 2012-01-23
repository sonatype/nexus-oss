/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class MainPage
{
    protected Selenium selenium;

    public final Selenium getSelenium()
    {
        return selenium;
    }

    public MainPage( Selenium selenium )
    {
        this.selenium = selenium;
        selenium.open( "/nexus" );

        for ( int i = 1; i < 50; i++ )
        {
            if ( i % 20 == 0 )
            {
                selenium.refresh();
            }

            if ( loginLinkAvailable() )
            {
                return;
            }

            try
            {
                selenium.waitForPageToLoad( "1000" );
            }
            catch ( Exception e )
            {
                // try again
            }
        }
    }

    public boolean loginLinkAvailable()
    {
        String text = selenium.getText( "head-link-r" );

        return "Log In".equals( text );
    }

    public boolean logoutLinkAvailable()
    {
        String text = selenium.getText( "head-link-r" );

        return "Log Out".equals( text );
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

        return new SchedulesConfigTab( selenium );
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
        adminPanel().logsAndConfigFilesClick();

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
        securityPanel().repositoryTargetsClick();

        return new RepoTargetTab( selenium );
    }

    public RotesTab openRoutes()
    {
        adminPanel().routingClick();

        return new RotesTab( selenium );
    }

    public void clickLogout()
    {
        if ( logoutLinkAvailable() )
        {
            selenium.click( "head-link-r" );
        }
    }

}
