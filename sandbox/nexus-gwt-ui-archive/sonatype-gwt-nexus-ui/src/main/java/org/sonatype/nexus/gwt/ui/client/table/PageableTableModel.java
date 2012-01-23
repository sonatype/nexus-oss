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


public class PageableTableModel extends AbstractTableModel implements TableModelListener {
    
    private TableModel model;
    
    private int pageSize;
    
    private int pageIndex;
    
    public PageableTableModel(TableModel model, int pageSize) {
        this.model = model;
        setPageSize(pageSize);
        model.addTableModelListener(this);
    }
    
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
        update();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        if (pageSize < 0 || pageIndex >= getPageCount()) {
            throw new IllegalArgumentException();
        }
        this.pageIndex = pageIndex;
        fireTableModelListeners();
    }
    
    public int getPageCount() {
        return model.getRowCount() / pageSize + (model.getRowCount() % pageSize == 0 ? 0 : 1);
    }

    public void firstPage() {
        setPageIndex(0);
    }

    public void lastPage() {
        setPageIndex(getPageCount() - 1);
    }

    public boolean nextPage() {
        if (pageIndex < getPageCount() - 1) {
            setPageIndex(pageIndex + 1);
            return true;
        }
        return false;
    }

    public boolean prevPage() {
        if (pageIndex > 0) {
            setPageIndex(pageIndex - 1);
            return true;
        }
        return false;
    }
    
    public Object getCell(int rowIndex, int colIndex) {
        return model.getCell(getRowIndex(rowIndex), colIndex);
    }

    public Object getRow(int rowIndex) {
        return model.getRow(getRowIndex(rowIndex));
    }

    public int getRowCount() {
        return Math.min(pageSize, model.getRowCount() - pageIndex * pageSize);
    }
    
    public int getColumnCount() {
        return model.getColumnCount();
    }

    protected int getRowIndex(int rowIndex) {
        return pageIndex * pageSize + rowIndex;
    }

    public void modelChanged(TableModel model) {
        update();
    }
    
    private void update() {
        pageIndex = 0;
        fireTableModelListeners();
    }

}
