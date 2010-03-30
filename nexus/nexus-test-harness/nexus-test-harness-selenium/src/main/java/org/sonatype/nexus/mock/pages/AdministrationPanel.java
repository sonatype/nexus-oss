package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class AdministrationPanel
    extends SidePanel
{
    public AdministrationPanel( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('st-nexus-config')" );
    }

    public boolean serverAvailable()
    {
        return isLinkAvailable( "Server" );
    }

    public boolean routingAvailable()
    {
        return isLinkAvailable( "Routing" );
    }

    public boolean scheduleTasksAvailable()
    {
        return isLinkAvailable( "Scheduled Tasks" );
    }

    public void clickScheduleTasks()
    {
        clickLink( "Scheduled Tasks" );
    }

    public boolean logAvailable()
    {
        return isLinkAvailable( "Log Configuration" );
    }

    public void serverClick()
    {
        clickLink( "Server" );
    }

    public void logClick()
    {
        clickLink( "Log Configuration" );
    }

    public void routingClick()
    {
        clickLink( "Routing" );
    }

    public boolean logsAndConfigFilesAvailable()
    {
        return isLinkAvailable( "System Files" );
    }

    public void logsAndConfigFilesClick()
    {
        clickLink( "System Files" );
    }
}
