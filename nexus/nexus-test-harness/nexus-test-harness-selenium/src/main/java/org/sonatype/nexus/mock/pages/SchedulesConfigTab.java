package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Menu;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class SchedulesConfigTab
    extends Component
{

    private MainPage mainPage;

    private ScheduleGrid scheduleGrid;

    public SchedulesConfigTab( Selenium selenium, MainPage mainPage )
    {
        super( selenium, "window.Ext.getCmp('schedules-config')" );
        this.mainPage = mainPage;

        scheduleGrid = new ScheduleGrid( selenium, this );
    }

    // schedule-add-btn

    public MainPage getMainPage()
    {
        return mainPage;
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
