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
