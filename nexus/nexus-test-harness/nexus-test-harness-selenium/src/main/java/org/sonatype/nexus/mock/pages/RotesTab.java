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
