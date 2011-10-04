/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
