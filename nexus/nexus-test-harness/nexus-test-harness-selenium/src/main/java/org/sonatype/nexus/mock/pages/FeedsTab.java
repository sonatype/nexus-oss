package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.NexusMockTestCase;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class FeedsTab
    extends Component
{

    private Grid feedCategorySelectorGrid;

    private Grid feedsGrid;

    public FeedsTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('feed-view-system-changes')" );

        feedCategorySelectorGrid = new Grid( selenium, expression + ".feedsGridPanel" );

        feedsGrid = new Grid( selenium, expression + ".grid" );
    }

    public FeedsTab selectCategory( String id )
    {
        // http://localhost:8084/nexus/service/local/feeds/authcAuthz
        feedCategorySelectorGrid.select( NexusMockTestCase.nexusBaseURL + "service/local/feeds/" + id );

        feedCategorySelectorGrid.waitToLoad();
        feedsGrid.waitToLoad();

        return this;
    }

    public FeedsTab selectFeed( int i )
    {
        // http://localhost:8084/nexus/service/local/feeds/authcAuthz
        feedCategorySelectorGrid.waitToLoad();
        feedsGrid.waitToLoad();

        feedsGrid.select( i );

        feedCategorySelectorGrid.waitToLoad();
        feedsGrid.waitToLoad();

        return this;
    }

    public String getFeedData( String fieldName )
    {
        return getEval( ".grid.getSelectionModel().getSelected().data." + fieldName );
    }

}
