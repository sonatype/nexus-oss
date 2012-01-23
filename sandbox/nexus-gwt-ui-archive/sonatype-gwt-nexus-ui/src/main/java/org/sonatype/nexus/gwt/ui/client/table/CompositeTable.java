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
