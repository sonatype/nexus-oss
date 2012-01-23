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
            Method getter =
                beans[rowIndex].getClass().getMethod("get" + props[colIndex]);
            return getter.invoke(beans[rowIndex]);
        } catch (Exception e) {
            //ignored
        }
        return null;
    }

}
