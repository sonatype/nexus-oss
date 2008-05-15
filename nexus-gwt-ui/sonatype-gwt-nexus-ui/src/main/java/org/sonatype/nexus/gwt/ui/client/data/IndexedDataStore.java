package org.sonatype.nexus.gwt.ui.client.data;

/**
 *
 * @author barath
 */
public interface IndexedDataStore extends DataStore {
    
    int getCount();
    
    Object get(int index);

}
