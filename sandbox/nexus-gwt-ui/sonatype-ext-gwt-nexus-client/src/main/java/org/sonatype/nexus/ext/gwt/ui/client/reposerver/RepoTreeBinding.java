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
package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.data.CallbackResponseHandler;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.ContentListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.Repository;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RepoTreeBinding {

    private TreeStore store;
    
    private TreeBinder<TreeModel> binder;
    
    private Repository selectedRepository;
    
    public RepoTreeBinding(Tree tree, final RepoServerInstance server) {
        
        DataProxy<ContentListResource, Object> proxy = new DataProxy<ContentListResource, Object>() {
            
            public void load(final DataReader<ContentListResource, Object> reader,
                    final ContentListResource parent, final AsyncCallback<Object> callback) {
                
                server.getRepositoryContent(parent, new CallbackResponseHandler<List<ContentListResource>>(callback) {

                    public void onSuccess(Response response, List<ContentListResource> children) {
                        for (ContentListResource child : children) {
                            child.setParent(parent);
                        }
                        callback.onSuccess(children);
                    }
                    
                });
            }
            
        };
        
        store = new TreeStore(new BaseTreeLoader(proxy));
        
        binder = new TreeBinder(tree, store);
        binder.setDisplayProperty("text");
        // TODO: Sort children by name
    }
    
    public Repository getSelectedRepository() {
        return selectedRepository;
    }
    
    public void selectRepository(final Repository repo) {
        if (repo == null) {
            return;
        }
        
        selectedRepository = repo;
        store.removeAll();
        
        ContentListResource root = new ContentListResource();
        root.setText(repo.getName());
        root.setResourceUri(repo.getResourceURI() + "/content");
        root.setLeaf(false);
        
        store.add(root, false);
        // TODO: Display the children of the root node
    }
    
    public void reload() {
        selectRepository(selectedRepository);
    }

    public TreeBinder<TreeModel> getBinder() {
        return binder;
    }
    
}
