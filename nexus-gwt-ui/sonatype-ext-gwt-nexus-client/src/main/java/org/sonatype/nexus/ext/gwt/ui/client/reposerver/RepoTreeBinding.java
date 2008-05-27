package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;
import org.sonatype.nexus.ext.gwt.ui.client.ResourceProxy;

import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.tree.Tree;

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
    
    private Tree tree;
    
    private ModelType modelType;
    
    private Variant variant;
    
    private TreeBinder binder;
    
    public RepoTreeBinding(Tree tree) {
        this(tree, DEFAULT_MODEL_TYPE, Variant.APPLICATION_XML);
    }
    
    public RepoTreeBinding(Tree tree, ModelType modelType, Variant variant) {
        this.tree = tree;
        this.modelType = modelType;
        this.variant = variant;
    }
    
    public void selectRepo(String repoName, String resourceUri) {
        Resource resource = new DefaultResource(Constants.HOST_URL + resourceUri);
        DataProxy proxy = new ResourceProxy(resource, variant);
        TreeLoader loader = new BaseTreeLoader(proxy, new XmlReader(modelType));
        TreeStore store = new TreeStore(loader);
        store.add(new RepoContentNode(repoName, resourceUri, false));
        binder = new TreeBinder(tree, store);
        loader.load();
    }

    public TreeBinder getBinder() {
        return binder;
    }
    
    private static class RepoContentNode extends BaseTreeModel {
        
        public RepoContentNode(Model model) {
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
