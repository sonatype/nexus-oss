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

import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Menu;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class SchedulesConfigTab
    extends Component
{

    private ScheduleGrid scheduleGrid;

    public SchedulesConfigTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('schedules-config')" );

        scheduleGrid = new ScheduleGrid( selenium, this );
    }

    // schedule-add-btn

    public SchedulesConfigFormTab addNewTask()
    {
        return scheduleGrid.newTask();
    }

    public ScheduleGrid getScheduleGrid()
    {
        return scheduleGrid;
    }

    public void contextMenuRefresh( int i )
    {
        Menu menu = scheduleGrid.openContextMenu( i, 1, "schedules-grid-ctx" );
        menu.click( "text", "Refresh" );

        scheduleGrid.waitToLoad();
    }

    public void contextMenuRun( String taskId )
    {
        Menu menu = scheduleGrid.openContextMenu( taskId, 1, "schedules-grid-ctx" );
        menu.click( "text", "Run" );

        new MessageBox( selenium ).clickYes();

        new Window( selenium ).waitFor();
    }

    public void contextMenuDelete( String taskId )
    {
        Menu menu = scheduleGrid.openContextMenu( taskId, 1, "schedules-grid-ctx" );
        menu.click( "text", "Delete" );

        new MessageBox( selenium ).clickYes();

        new Window( selenium ).waitFor();
    }

}
