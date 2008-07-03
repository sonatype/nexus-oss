package org.sonatype.nexus.gwt.ui.client.repository;

import java.util.Iterator;

import org.sonatype.nexus.gwt.ui.client.data.DataStore;
import org.sonatype.nexus.gwt.ui.client.data.DataStoreListener;
import org.sonatype.nexus.gwt.ui.client.data.IterableDataStore;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.ListBox;

/**
 *
 * @author barath
 */
public class RepositoriesListBox extends ListBox implements DataStoreListener {
    
    private IterableDataStore dataStore;
    
    public RepositoriesListBox(IterableDataStore dataStore) {
        this(dataStore, false);
    }
    
    public RepositoriesListBox(IterableDataStore dataStore, boolean isMultipleSelect) {
        super(isMultipleSelect);
        this.dataStore = dataStore;
        
        refreshed(dataStore);
        dataStore.addDataStoreListener(this);
    }
    
    public void refreshed(DataStore sender) {
        clear();
        for (Iterator i = dataStore.iterator(); i.hasNext();) {
            JSONObject repo = (JSONObject) i.next();
            String repoType = repo.get("repoType").isString().stringValue();
            if (!"virtual".equals(repoType)) {
                String repoId = repo.get("resourceURI").isString().stringValue();
                repoId = repoId.substring(repoId.lastIndexOf("/") + 1);
                addItem(repoId, repoId);
            }
        }
    }

}
