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
package org.sonatype.nexus.gwt.ui.client.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.nexus.gwt.ui.client.JSONUtil;

import com.google.gwt.json.client.JSONArray;

/**
 *
 * @author barath
 */
public class JSONArrayDataStore extends AbstractDataStore
        implements IndexedDataStore, IterableDataStore {

    private List elements = new ArrayList();

    
    public JSONArrayDataStore() {
    }
    
    public JSONArrayDataStore(JSONArray elements) {
        this(JSONUtil.jsonArrayToList(elements));
    }

    public JSONArrayDataStore(List elements) {
        if (elements != null) {
            this.elements = elements;
        }
    }
    
    public Object getData() {
        return getElements();
    }

    public List getElements() {
        return elements;
    }

    public void setElements(List elements) {
        this.elements = (elements == null ? new ArrayList(0) : elements);
        fireDataStoreListeners();
    }
    
    public void setElements(JSONArray elements) {
        if (elements != null) {
            this.elements = JSONUtil.jsonArrayToList(elements);
        } else {
            this.elements = new ArrayList(0);
        }
        fireDataStoreListeners();
    }

    public Object get(int index) {
        return elements.get(index);
    }

    public int getCount() {
        return elements.size();
    }

    public Iterator iterator() {
        return elements.iterator();
    }
    
}
