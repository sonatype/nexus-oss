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
import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class ScheduleGrid
    extends Grid
{

    private Button refreshButton;

    private Button addButton;

    private Button deleteButton;

    private SchedulesConfigTab schedulesConfigTab;

    public ScheduleGrid( Selenium selenium, SchedulesConfigTab schedulesConfigTab )
    {
        super( selenium, "window.Ext.getCmp('st-schedules-grid')" );
        this.schedulesConfigTab = schedulesConfigTab;

        refreshButton = new Button( selenium, "window.Ext.getCmp('schedule-refresh-btn')" );
        addButton = new Button( selenium, "window.Ext.getCmp('schedule-add-btn')" );
        deleteButton = new Button( selenium, "window.Ext.getCmp('schedule-delete-btn')" );

    }

    public Button getAddButton()
    {
        return addButton;
    }

    public Button getDeleteButton()
    {
        return deleteButton;
    }

    public Button getRefreshButton()
    {
        return refreshButton;
    }

    public SchedulesConfigFormTab newTask()
    {
        schedulesConfigTab.getScheduleGrid().waitToLoad();

        addButton.click();

        return new SchedulesConfigFormTab( selenium );
    }

    public ScheduleGrid refresh()
    {
        getRefreshButton().click();

        waitToLoad();

        return this;
    }

    public MessageBox deleteTask()
    {
        getDeleteButton().click();

        return new MessageBox(selenium);
    }

}
