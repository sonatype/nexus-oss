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

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class RepoTargetTab
    extends Component
{

    private Button addButton;

    private Button deleteButton;

    private Grid grid;

    private Button refreshButton;

    public RepoTargetTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('targets-config')" );

        this.grid = new Grid( selenium, "window.Ext.getCmp('st-repoTargets-grid')" );

        this.refreshButton = new Button( selenium, "window.Ext.getCmp('repoTarget-refresh-btn')" );
        this.addButton = new Button( selenium, "window.Ext.getCmp('repoTarget-add-btn')" );
        this.deleteButton = new Button( selenium, "window.Ext.getCmp('repoTarget-delete-btn')" );
    }

    public RepoTargetForm addRepoTarget()
    {
        this.addButton.click();

        return new RepoTargetForm( selenium, "window.Ext.getCmp('targets-config')" );
    }

    public final Button getAddButton()
    {
        return addButton;
    }

    public final Button getDeleteButton()
    {
        return deleteButton;
    }

    public final Grid getGrid()
    {
        return grid;
    }

    public final Button getRefreshButton()
    {
        return refreshButton;
    }

    public void refresh()
    {
        refreshButton.click();

        grid.waitToLoad();
    }

    public RepoTargetForm select( String targetId )
    {
        grid.select( targetId );

        return new RepoTargetForm( selenium, "window.Ext.getCmp('targets-config')" );
    }

    public MessageBox delete()
    {
        deleteButton.click();

        return new MessageBox( selenium );
    }

}
