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
