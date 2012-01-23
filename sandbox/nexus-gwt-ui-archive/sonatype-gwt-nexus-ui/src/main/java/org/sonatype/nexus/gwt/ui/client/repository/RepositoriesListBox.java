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
