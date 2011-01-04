/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.NexusMockTestCase;
import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Menu;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesTab
    extends AbstractTab
{

    public static final String REPOSITORIES_ST = "window.Ext.getCmp('view-repositories')";

    private MainPage mainPage;

    private Button typeButton;

    public RepositoriesTab( Selenium selenium, MainPage mainPage )
    {
        super( selenium, REPOSITORIES_ST );
        this.mainPage = mainPage;

        this.typeButton = new Button( selenium, expression + ".browseTypeButton" );
    }

    public RepositoriesConfigurationForm addHostedRepo()
    {
        addButton.click();

        addMenu.click( "text", "Hosted Repository" );

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesConfigurationForm addProxyRepo()
    {
        addButton.click();

        addMenu.click( "text", "Proxy Repository" );

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesConfigurationForm addVirtualRepo()
    {
        addButton.click();

        addMenu.click( "text", "Virtual Repository" );

        return new RepositoriesConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public RepositoriesEditTabs select( String repoId, RepoKind kind )
    {
        this.grid.waitToLoad();

        if ( RepoKind.GROUP.equals( kind ) )
        {
            this.grid.select( NexusMockTestCase.nexusBaseURL + "service/local/repo_groups/" + repoId );
        }
        else
        {
            this.grid.select( NexusMockTestCase.nexusBaseURL + "service/local/repositories/" + repoId );
        }

        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            //
        }

        return new RepositoriesEditTabs( selenium, kind );
    }

    public MainPage getMainPage()
    {
        return mainPage;
    }

    public Button getTypeButton()
    {
        return typeButton;
    }

    public boolean contains( String repoId )
    {
        return this.grid.contains( getUiId( repoId ) );
    }

    private String getUiId( String repoId )
    {
        return NexusMockTestCase.nexusBaseURL + "service/local/repositories/" + repoId;
    }

    public GroupConfigurationForm addGroup()
    {
        addButton.click();

        addMenu.click( "text", "Repository Group" );

        return new GroupConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public void contextMenuExpireCache( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Expire Cache" );
    }

    public void contextMenuReindex( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Repair Index" );

        new Window( selenium ).waitFor();
    }

    public void contextMenuIncrementalReindex( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Update ReIndex" );
    }

    public void contextMenuPutOutOfService( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Put Out of Service" );
    }

    public void contextMenuPutInService( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Put In Service" );
    }

    public String getStatus( String repoId )
    {
        String uiStatus =
            selenium.getEval( grid.getExpression() + ".store.data.get( '" + getUiId( repoId ) + "' ).data.displayStatus" );
        return uiStatus;
    }

    public void contextMenuRebuildMetadata( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Rebuild Metadata" );
    }

    public void contextMenuBlockProxy( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Block Proxy" );
    }

    public void contextMenuAllowProxy( String id )
    {
        Menu menu = grid.openContextMenu( getUiId( id ), 1, "grid-context-menu" );
        menu.click( "text", "Allow Proxy" );
    }

}
