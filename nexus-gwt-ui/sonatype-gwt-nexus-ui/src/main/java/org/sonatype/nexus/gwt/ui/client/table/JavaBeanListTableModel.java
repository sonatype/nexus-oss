package org.sonatype.nexus.gwt.ui.client.table;

import java.lang.reflect.Method;


public class JavaBeanListTableModel extends AbstractTableModel {
    
    private Object[] beans;
    
    private String[] props;
    
    public JavaBeanListTableModel(Object[] beans, String[] props) {
        this.beans = beans;
        this.props = props;
        if (beans == null) {
            beans = new Object[0];
        }
        if (props == null) {
            props = new String[0];
        }
    }

    public int getColumnCount() {
        return props.length;
    }

    public Object getRow(int rowIndex) {
        return beans[rowIndex];
    }

    public int getRowCount() {
        return beans.length;
    }

    public Object getCell(int rowIndex, int colIndex) {
        try {
            Method getter = beans[rowIndex].getClass()
                    .getMethod("get" + props[colIndex], null);
            return getter.invoke(beans[rowIndex], null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
