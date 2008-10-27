package org.sonatype.nexus.gwt.ui.client.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractTableModel implements TableModel {
    
    private List listeners = new ArrayList();

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
    
    protected void fireTableModelListeners() {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            TableModelListener l = (TableModelListener) i.next();
            l.modelChanged(this);
        }
    }

}
