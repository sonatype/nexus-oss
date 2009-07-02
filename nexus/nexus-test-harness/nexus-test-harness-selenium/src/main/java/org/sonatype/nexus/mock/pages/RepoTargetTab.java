package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class RepoTargetTab
    extends Component
{

    private Button addButton;

    private Button deleteButton;

    private Grid grid;

    private Button refreshButton;

    public RepoTargetTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('targets-config')" );

        this.grid = new Grid( selenium, "window.Ext.getCmp('st-repoTargets-grid')" );

        this.refreshButton = new Button( selenium, "window.Ext.getCmp('repoTarget-refresh-btn')" );
        this.addButton = new Button( selenium, "window.Ext.getCmp('repoTarget-add-btn')" );
        this.deleteButton = new Button( selenium, "window.Ext.getCmp('repoTarget-delete-btn')" );
    }

    public RepoTargetForm addRepoTarget()
    {
        this.addButton.click();

        return new RepoTargetForm( selenium, "window.Ext.getCmp('targets-config')" );
    }

    public final Button getAddButton()
    {
        return addButton;
    }

    public final Button getDeleteButton()
    {
        return deleteButton;
    }

    public final Grid getGrid()
    {
        return grid;
    }

    public final Button getRefreshButton()
    {
        return refreshButton;
    }

    public void refresh()
    {
        refreshButton.click();

        grid.waitToLoad();
    }

    public RepoTargetForm select( String targetId )
    {
        grid.select( targetId );

        return new RepoTargetForm( selenium, "window.Ext.getCmp('targets-config')" );
    }

    public MessageBox delete()
    {
        deleteButton.click();

        return new MessageBox( selenium );
    }

}
