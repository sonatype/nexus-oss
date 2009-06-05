package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Component;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesTab extends Component
{

    private MainPage mainPage;

    public RepositoriesTab( Selenium selenium, MainPage mainPage )
    {
        super( selenium, "window.Ext.getCmp('st-repositories')" );
        this.mainPage = mainPage;

        //TODO need buttons

    }

}
