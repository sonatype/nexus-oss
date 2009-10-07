package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class RotesTab
    extends Component
{

    private Grid grid;

    private Button refresh;

    private Button add;

    private Button delete;

    public RotesTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('routes-config')" );

        this.grid = new Grid( selenium, "window.Ext.getCmp('st-routes-grid')" );

        this.refresh = new Button( selenium, "window.Ext.getCmp('route-refresh-btn')" );
        this.add = new Button( selenium, "window.Ext.getCmp('route-add-btn')" );
        this.delete = new Button( selenium, "window.Ext.getCmp('route-delete-btn')" );
    }

    public RouteForm addRoute()
    {
        add.click();

        return new RouteForm( selenium, "window.Ext.getCmp('route-config-forms')" );
    }

    public void refresh()
    {
        this.refresh.click();

        this.grid.waitToLoad();
    }

    public final Grid getGrid()
    {
        return grid;
    }

    public final Button getRefresh()
    {
        return refresh;
    }

    public final Button getAdd()
    {
        return add;
    }

    public final Button getDelete()
    {
        return delete;
    }

    public RouteForm select( String routeId )
    {
        grid.select( routeId );

        return new RouteForm( selenium, "window.Ext.getCmp('route-config-forms')" );
    }

    public MessageBox delete()
    {
        delete.click();

        return new MessageBox( selenium );
    }

}
