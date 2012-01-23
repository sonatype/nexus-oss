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
