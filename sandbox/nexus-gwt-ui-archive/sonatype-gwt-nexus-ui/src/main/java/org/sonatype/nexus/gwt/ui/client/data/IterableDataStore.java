package org.sonatype.nexus.gwt.ui.client.data;

import java.util.Iterator;

/**
 *
 * @author barath
 */
public interface IterableDataStore extends DataStore {
    
    Iterator iterator();

}
