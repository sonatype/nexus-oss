package org.sonatype.nexus.gwt.ui.client.table;

public interface TableModel {
    
    int getRowCount();
    
    int getColumnCount();
    
    Object getCell(int rowIndex, int colIndex);
    
    Object getRow(int rowIndex);
    
    void addTableModelListener(TableModelListener l);

    void removeTableModelListener(TableModelListener l);

}
