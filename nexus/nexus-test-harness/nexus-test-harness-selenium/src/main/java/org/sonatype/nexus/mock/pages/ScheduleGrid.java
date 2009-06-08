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
        addButton.click();

        return new SchedulesConfigFormTab( selenium );
    }

    public ScheduleGrid getRefresh()
    {
        getRefreshButton().click();

        return this;
    }

    public MessageBox deleteTask()
    {
        getDeleteButton().click();

        return new MessageBox(selenium);
    }

}
