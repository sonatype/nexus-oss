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
