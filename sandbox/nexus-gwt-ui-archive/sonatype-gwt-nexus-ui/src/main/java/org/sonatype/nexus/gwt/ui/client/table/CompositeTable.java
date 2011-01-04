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
package org.sonatype.nexus.gwt.ui.client.table;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 *
 * @author kurai
 */
public class CompositeTable extends Composite {

    private Table table;

    private ToolBar toolBar;

    public CompositeTable(String name, TableModel model, String[] cols, ConstantsWithLookup constants) {
        this(name, model, new DefaultColumnModel(cols, constants), 5);
    }
    
    public CompositeTable(String name, TableModel model, String[] headers) {
        this(name, model, new DefaultColumnModel(headers), 5);
    }

    public CompositeTable(String name, TableModel model, ColumnModel columnModel, int pageSize) {
        PageableTableModel pmodel = new PageableTableModel(model, pageSize);

        // Setup the table.
        table = new Table(pmodel, 0);
        table.setColumnModel(columnModel);
        table.refresh();
        table.setStyleName(name);

        //Setup toolbar and navbar
        PagingNavBar navBar = new PagingNavBar(pmodel);
        toolBar = new ToolBar();
        table.addSelectionListener(toolBar);
        VerticalPanel panel = new VerticalPanel();
        HorizontalPanel bar = new HorizontalPanel();
        bar.add(toolBar);
        bar.add(navBar);
        bar.setStyleName("top-bar");
        panel.add(bar);
        panel.add(table);

        initWidget(panel);
    }

    public Table getTable() {
        return table;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }
    
}
