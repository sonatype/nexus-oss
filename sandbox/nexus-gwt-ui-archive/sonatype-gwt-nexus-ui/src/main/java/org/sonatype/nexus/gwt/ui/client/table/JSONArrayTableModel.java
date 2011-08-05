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

import org.sonatype.nexus.gwt.ui.client.JSONUtil;
import org.sonatype.nexus.gwt.ui.client.data.DataStore;
import org.sonatype.nexus.gwt.ui.client.data.DataStoreListener;
import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * 
 *
 * @author barath
 */
public class JSONArrayTableModel extends AbstractTableModel implements DataStoreListener {
    
    private String[] props;
    
    private JSONArrayDataStore dataStore;
    
    public JSONArrayTableModel(JSONArrayDataStore dataStore, String[] props) {
        if (props == null) {
            throw new NullPointerException("props is null");
        }
        if (dataStore == null) {
            throw new NullPointerException("dataStore is null");
        }
        
        this.props = props;
        this.dataStore = dataStore;
        
        dataStore.addDataStoreListener(this);
    }
    
    public int getColumnCount() {
        return props.length;
    }

    public Object getRow(int rowIndex) {
        return dataStore.getElements().get(rowIndex);
    }

    public int getRowCount() {
        return dataStore.getElements().size();
    }
    
    public Object getCell(int rowIndex, int colIndex) {
        JSONObject obj = (JSONObject) getRow(rowIndex);
        
        JSONValue val = JSONUtil.getValue(obj, props[colIndex]);
        
        if (val != null) {
            if (val.isString() != null) {
                return val.isString().stringValue();
            } else if (val.isBoolean() != null) {
                return Boolean.valueOf(val.isBoolean().booleanValue());
            } else if (val.isNumber() != null) {
                return new Double(val.isNumber().doubleValue());
            } else if (val.isNull() != null) {
                return null;
            } else {
                return val;
            }
        }
        
        return null;
    }

    public void refreshed(DataStore sender) {
        fireTableModelListeners();
    }
    
}
