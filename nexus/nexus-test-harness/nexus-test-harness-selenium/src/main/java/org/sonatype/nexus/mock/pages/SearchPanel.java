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
