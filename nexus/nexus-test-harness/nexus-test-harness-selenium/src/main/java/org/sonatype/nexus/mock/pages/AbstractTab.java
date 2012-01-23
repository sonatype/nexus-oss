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
import org.sonatype.nexus.mock.components.Menu;

import com.thoughtworks.selenium.Selenium;

public class AbstractTab
    extends Component
{

    protected Grid grid;

    protected Button refreshButton;

    protected Button addButton;

    protected Button deleteButton;

    protected Menu addMenu;

    public AbstractTab( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.grid = new Grid( selenium, expression + ".gridPanel" );

        this.refreshButton = new Button( selenium, expression + ".refreshButton" );
        this.deleteButton = new Button( selenium, expression + ".toolbarDeleteButton" );

        this.addButton = new Button( selenium, expression + ".toolbarAddButton" );
        this.addMenu = new Menu( selenium, addButton.getExpression() + ".menu" );
    }

    public MessageBox delete()
    {
        this.deleteButton.click();

        return new MessageBox( selenium );
    }

    public void refresh()
    {
        this.refreshButton.click();

        this.grid.waitToLoad();
    }

    public Grid getGrid()
    {
        return grid;
    }

    public Button getRefreshButton()
    {
        return refreshButton;
    }

    public Button getAddButton()
    {
        return addButton;
    }

    public Button getDeleteButton()
    {
        return deleteButton;
    }

    public Menu getAddMenu()
    {
        return addMenu;
    }

}
