package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

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

}
