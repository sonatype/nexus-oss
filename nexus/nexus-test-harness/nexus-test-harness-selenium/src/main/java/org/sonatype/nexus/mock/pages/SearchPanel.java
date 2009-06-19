package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.TriggerField;

import com.thoughtworks.selenium.Selenium;

public class SearchPanel
    extends SidePanel
{
    private TriggerField quickSearch;

    public SearchPanel( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('st-nexus-search')" );

        quickSearch = new TriggerField(selenium, "window.Ext.getCmp('quick-search--field')");
    }

    public boolean advancedSearchAvailable()
    {
        return isLinkAvailable( "Advanced Search" );
    }

    public SearchTab clickAdvancedSearch()
    {
        clickLink( "Advanced Search" );

        return new SearchTab( selenium );
    }

    public SearchTab search( String query )
    {
        quickSearch.type( query );

        quickSearch.clickTrigger();

        SearchTab searchTab = new SearchTab(selenium);

        searchTab.getGrid().waitToLoad();

        return searchTab;
    }

}
