package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RepoTreeBinding {

    private static final ModelType DEFAULT_MODEL_TYPE = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.ContentListResource";
            addField("name", "text");
            addField("resourceUri");
            addField("leaf");
        }
    };
    
    private Variant variant;
    
    private TreeStore store;
    
    private TreeBinder<TreeModel> binder;
    
    private ModelData selectedRepo;
    
    public RepoTreeBinding(Tree tree) {
        this(tree, DEFAULT_MODEL_TYPE, Variant.APPLICATION_XML);
    }
    
    public RepoTreeBinding(Tree tree, ModelType modelType, Variant variant) {
        this.variant = variant;
        
        TreeLoader loader =
            new BaseTreeLoader(new RepoContentProxy(), new XmlReader(modelType));
        store = new TreeStore(loader);
        binder = new TreeBinder(tree, store);
        binder.setDisplayProperty("name");
        // TODO: Sort children by name
    }
    
    public void selectRepo(final ModelData repo) {
        if (repo == null) {
            return;
        }
        selectedRepo = repo;
        store.removeAll();
        String repoName = (String) repo.get("name");
        String resourceURI = (String) repo.get("resourceURI") + "/content";
        store.add(new RepoContentNode(repoName, resourceURI, false));
        // TODO: Display the children of the root node
    }
    
    public void reload() {
        selectRepo(selectedRepo);
    }

    public TreeBinder<TreeModel> getBinder() {
        return binder;
    }
    
    private class RepoContentProxy implements DataProxy<RepoContentNode, Object> {
        
        public void load(final DataReader<RepoContentNode, Object> reader,
                RepoContentNode parent, final AsyncCallback<Object> callback) {

            String url = Constants.HOST + parent.getResourceUri();

            new DefaultResource(url).get(new RequestCallback() {

                public void onError(Request request, Throwable exception) {
                    callback.onFailure(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    List<RepoContentNode> list = new ArrayList<RepoContentNode>();
                    ListLoadResult<ModelData> result =
                        (ListLoadResult<ModelData>) reader.read(null, response.getText());
                    for (ModelData model : result.getData()) {
                        list.add(new RepoContentNode(model));
                    }
                    callback.onSuccess(list);
                }

            }, variant);
        }
        
    }
    
    private static class RepoContentNode extends BaseTreeModel {
        
        public RepoContentNode(ModelData model) {
            set("name", model.get("name"));
            set("resourceUri", model.get("resourceUri"));
            set("leaf", Boolean.valueOf((String) model.get("leaf")));
        }
        
        public RepoContentNode(String name, String resourceUri, boolean leaf) {
            set("name", name);
            set("resourceUri", resourceUri);
            set("leaf", leaf);
        }
        
        public String getName() {
            return (String) get("name");
        }
        
        public String getResourceUri() {
            return (String) get("resourceUri");
        }
        
        public boolean isLeaf() {
            return (Boolean) get("leaf");
        }
        
    }

}
