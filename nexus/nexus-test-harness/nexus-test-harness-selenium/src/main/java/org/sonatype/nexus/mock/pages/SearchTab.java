/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;
import org.sonatype.nexus.mock.components.SearchField;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class SearchTab
    extends Component
{

    private SearchField allSearch;

    private Button searchSelector;

    private Button keywordButton;

    private Button classnameButton;

    private Button gavButton;

    private Button checksumButton;

    private TextField gavGroup;

    private TextField gavArtifact;

    private TextField gavVersion;

    private TextField gavPackaging;

    private TextField gavClassifier;

    private Grid grid;

    public SearchTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('nexus-search')" );

        searchSelector = new Button( selenium, expression + ".searchToolbar.items.first()" );
        keywordButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[0].el" );
        keywordButton.idFunction = ".dom.id";
        classnameButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[1].el" );
        classnameButton.idFunction = ".dom.id";
        gavButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[2].el" );
        gavButton.idFunction = ".dom.id";
        checksumButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[3].el" );
        checksumButton.idFunction = ".dom.id";

        allSearch = new SearchField( selenium, expression + ".searchField" );

        gavGroup = new TextField( selenium, "window.Ext.getCmp('gavsearch-group')" );
        gavArtifact = new TextField( selenium, "window.Ext.getCmp('gavsearch-artifact')" );
        gavVersion = new TextField( selenium, "window.Ext.getCmp('gavsearch-version')" );
        gavPackaging = new TextField( selenium, "window.Ext.getCmp('gavsearch-packaging')" );
        gavClassifier = new TextField( selenium, "window.Ext.getCmp('gavsearch-classifier')" );
        gavButton = new Button( selenium, expression + ".searchToolbar.items.last()" );

        grid = new Grid( selenium, expression + ".grid" );
    }

    public SearchField getAllSearch()
    {
        return allSearch;
    }

    public Button getSearchSelector()
    {
        return searchSelector;
    }

    public Button getKeywordButton()
    {
        return keywordButton;
    }

    public Button getClassnameButton()
    {
        return classnameButton;
    }

    public Button getGavButton()
    {
        return gavButton;
    }

    public Button getChecksumButton()
    {
        return checksumButton;
    }

    public TextField getGavGroup()
    {
        return gavGroup;
    }

    public TextField getGavArtifact()
    {
        return gavArtifact;
    }

    public TextField getGavVersion()
    {
        return gavVersion;
    }

    public TextField getGavPackaging()
    {
        return gavPackaging;
    }

    public TextField getGavClassifier()
    {
        return gavClassifier;
    }

    public Grid getGrid()
    {
        return grid;
    }

    private void doSeach( Button b, String q )
    {
        searchSelector.click();
        b.clickNoWait();

        allSearch.waitToLoad();
        allSearch.type( q );
        allSearch.clickSearch();

        grid.waitToLoad();
    }

    public SearchTab keywordSearch( String q )
    {
        doSeach( keywordButton, q );

        return this;
    }

    public SearchTab classnameSearch( String q )
    {
        doSeach( classnameButton, q );

        return this;
    }

    public SearchTab checksumSearch( String q )
    {
        doSeach( checksumButton, q );

        return this;
    }

    public ArtifactInformationPanel select( int i )
    {
        grid.select( i );

        return new ArtifactInformationPanel( selenium, expression + ".artifactContainer" );
    }

}
