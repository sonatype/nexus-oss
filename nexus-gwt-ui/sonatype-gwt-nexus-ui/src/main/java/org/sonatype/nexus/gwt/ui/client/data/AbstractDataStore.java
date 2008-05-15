package org.sonatype.nexus.gwt.ui.client.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 * @author barath
 */
public abstract class AbstractDataStore implements DataStore {
    
    private List listeners = new ArrayList();

    public void addDataStoreListener(DataStoreListener l) {
        listeners.add(l);
    }

    public void removeDataStoreListener(DataStoreListener l) {
        listeners.remove(l);
    }
    
    protected void fireDataStoreListeners() {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            DataStoreListener l = (DataStoreListener) i.next();
            l.refreshed(this);
        }
    }

}
