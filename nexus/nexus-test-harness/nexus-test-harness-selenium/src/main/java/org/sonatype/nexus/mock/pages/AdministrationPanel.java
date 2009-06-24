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

    public boolean repositoryTargetsAvailable()
    {
        return isLinkAvailable( "Repository Targets" );
    }

    public boolean logAvailable()
    {
        return isLinkAvailable( "Log" );
    }

    public void serverClick()
    {
        clickLink( "Server" );
    }

    public void logClick()
    {
        clickLink( "Log" );
    }
}
