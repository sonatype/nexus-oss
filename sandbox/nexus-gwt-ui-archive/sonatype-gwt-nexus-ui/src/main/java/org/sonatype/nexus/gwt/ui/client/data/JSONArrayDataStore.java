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
