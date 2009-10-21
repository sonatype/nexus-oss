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
        keywordButton.idFunction = ".id";
        classnameButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[1].el" );
        classnameButton.idFunction = ".id";
        gavButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[2].el" );
        gavButton.idFunction = ".id";
        checksumButton = new Button( selenium, searchSelector.getExpression() + ".menu.items.items[3].el" );
        checksumButton.idFunction = ".id";

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
