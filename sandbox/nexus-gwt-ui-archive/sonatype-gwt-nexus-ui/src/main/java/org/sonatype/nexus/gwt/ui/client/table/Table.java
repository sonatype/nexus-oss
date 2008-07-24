package org.sonatype.nexus.gwt.ui.client.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;

public class Table extends FlexTable implements TableModelListener, TableListener {
    
    private TableModel model;
    
    private TableView view;

    private ColumnModel columnModel;
    
    private List selectionListeners = new ArrayList();

    private int visibleRowCount;
    
    private boolean rowSelectionEnabled = true;
    
    private int selectedRowIndex = -1;
    
    public Table(TableModel model, int visibleRowCount) {
        this(model, new DefaultTableView(), new DefaultColumnModel(null), visibleRowCount);
    }
    
    public Table(TableModel model, TableView view, ColumnModel columnModel) {
        this(model, view, columnModel, 0);
    }
    
    public Table(TableModel model, TableView view, ColumnModel columnModel, int visibleRowCount) {
        this.visibleRowCount = visibleRowCount;
        this.model = model;
        this.view = view;
        this.columnModel = columnModel;
        
        setStyleName(view.getTableStyle());
        addTableListener(this);
        model.addTableModelListener(this);
        refresh();
    }
    
    public TableModel getModel() {
        return model;
    }

    public void setModel(TableModel model) {
        this.model = model;
    }

    public ColumnModel getColumnModel() {
        return columnModel;
    }

    public void setColumnModel(ColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    public TableView getView() {
        return view;
    }

    public void setView(TableView view) {
        this.view = view;
    }

    public boolean isRowSelectionEnabled() {
        return rowSelectionEnabled;
    }

    public void setRowSelectionEnabled(boolean rowSelectionEnabled) {
        this.rowSelectionEnabled = rowSelectionEnabled;
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex;
    }
    
    public void addSelectionListener(SelectionListener l) {
        selectionListeners.add(l);
    }

    public void removeSelectionListener(SelectionListener l) {
        selectionListeners.remove(l);
    }
    
    protected void fireSelectionListeners() {
        for (Iterator i = selectionListeners.iterator(); i.hasNext();) {
            SelectionListener l = (SelectionListener) i.next();
            l.selectionChanged(this);
        }
    }

    public Object getSelectedRow() {
        if (selectedRowIndex > -1) {
            return model.getRow(selectedRowIndex);
        }
        return null;
    }

    public void selectRow(int rowIndex) {
        if (rowIndex != selectedRowIndex && rowIndex < model.getRowCount()) {
            getRowFormatter().removeStyleName(selectedRowIndex + 1, view.getSelectedRowStyle());
            if (rowIndex > -1) {
                getRowFormatter().addStyleName(rowIndex + 1, view.getSelectedRowStyle());
            }
            selectedRowIndex = rowIndex;
            fireSelectionListeners();
        }
    }

    public void modelChanged(TableModel sender) {
        refresh();
    }
    
    public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
        if (rowSelectionEnabled && row > 0) {
            if (selectedRowIndex == row - 1) {
                selectRow(-1);
            } else {
                selectRow(row - 1);
            }
        }
    }
    
    public void refresh() {
        for (int j = 0; j < model.getColumnCount(); j++) {
            setWidget(0, j, columnModel.getHeader(j));
        	getColumnFormatter().setStyleName(j, view.getColumnStyle(j));
        }
        getRowFormatter().setStyleName(0, view.getRowStyle(0, null));

        for (int i = 0; i < model.getRowCount(); i++) {
            getRowFormatter().setStyleName(i + 1,
                    view.getRowStyle(i + 1, model.getRow(i)));
            for (int j = 0; j < model.getColumnCount(); j++) {
                // setting cell value
                Object renderedCellValue = columnModel.getCellRenderer(j)
                        .renderCell(i, j, model.getCell(i, j));
                if (renderedCellValue instanceof Widget) {
                    setWidget(i + 1, j, (Widget) renderedCellValue);
                } else {
                    setText(i + 1, j, (String) renderedCellValue);
                }
                
                // setting cell style
                String cellStyle = view.getCellStyle(i + 1, j, model.getCell(i, j));
                if (cellStyle != null) {
                    getCellFormatter().setStyleName(i + 1, j, cellStyle);
                }
            }
        }
        
        if (visibleRowCount <= 0) {
            while (getRowCount() > model.getRowCount() + 1) {
                removeRow(getRowCount() - 1);
            }
        } else if (getRowCount() > model.getRowCount() + 1) {
            for (int i = model.getRowCount() + 1; i < getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    setHTML(i, j, "&nbsp;");
                }
            }
        } else if (getRowCount() < visibleRowCount + 1) {
            for (int i = getRowCount(); i < visibleRowCount + 1; i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    setHTML(i, j, "&nbsp;");
                }
            }
        }
        
        selectRow(-1);
    }

}
