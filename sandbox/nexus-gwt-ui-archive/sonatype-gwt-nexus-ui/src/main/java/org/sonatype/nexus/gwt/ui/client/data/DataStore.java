package org.sonatype.nexus.gwt.ui.client.data;

/**
 * 
 *
 * @author barath
 */
public interface DataStore {
    
    void addDataStoreListener(DataStoreListener l);

    void removeDataStoreListener(DataStoreListener l);
    
    Object getData();
    
}
