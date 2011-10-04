/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
