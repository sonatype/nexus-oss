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
