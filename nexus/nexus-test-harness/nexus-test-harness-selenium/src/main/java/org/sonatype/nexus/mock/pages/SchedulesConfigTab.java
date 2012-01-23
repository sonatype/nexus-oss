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
