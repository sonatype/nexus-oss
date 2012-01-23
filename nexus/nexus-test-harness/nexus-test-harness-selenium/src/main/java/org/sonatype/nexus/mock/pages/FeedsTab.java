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
