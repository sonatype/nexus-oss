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
