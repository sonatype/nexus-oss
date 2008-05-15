package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.LoadResult;
import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.viewer.AsyncContentCallback;
import com.extjs.gxt.ui.client.viewer.ModelLabelProvider;
import com.extjs.gxt.ui.client.viewer.TreeContentProvider;
import com.extjs.gxt.ui.client.viewer.TreeViewer;
import com.extjs.gxt.ui.client.viewer.Viewer;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

@SuppressWarnings("unchecked")
public class RepoTreeViewer extends TreeViewer {
    
    private ModelType modelType = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.ContentListResource";
            addField("name", "text");
            addField("resourceUri");
            addField("leaf");
        }
    };
    
    private Variant variant = Variant.APPLICATION_XML;
    
    private String repoName;
    
    private String resourceUri;
    
    public RepoTreeViewer(Tree tree) {
        super(tree);
        setContentProvider(new RepoTreeContentProvider());
        setLabelProvider(new ModelLabelProvider());
    }
    
    public RepoTreeViewer(Tree tree, ModelType modelType, Variant variant) {
        this(tree);
        this.modelType = modelType;
        this.variant = variant;
    }
    
    public void selectRepo(String repoName, String resourceUri) {
        this.repoName = repoName;
        this.resourceUri = resourceUri;
        setInput(new RepoContentNode("ROOT", null, false));
    }
    
    private class RepoTreeContentProvider implements TreeContentProvider {
        
        public Object getParent(Object element) {
            return ((TreeModel)element).getParent();
        }

        public boolean hasChildren(Object parent) {
            return !((RepoContentNode) parent).isLeaf();
        }

        public void getChildren(final Object parent,
                final AsyncContentCallback<Object> callback) {
            
            final List<Object> children = new ArrayList<Object>();
            
            RepoContentNode parentNode = (RepoContentNode) parent;
            
            if (parentNode.getResourceUri() == null) {
                children.add(new RepoContentNode(repoName, resourceUri, false));
                callback.setElements(children);
            } else {
                Resource resource = new DefaultResource(
                       Constants.HOST_URL + parentNode.getResourceUri());
                resource.get(new RequestCallback() {
                    public void onError(Request request, Throwable t) {
                        callback.setElements(children);
                    }
                    public void onResponseReceived(Request request, Response response) {
                        DataReader reader = new XmlReader(modelType);
                        LoadResult<Model> result = reader.read(null, response.getText());
                        for (Model model : (List<Model>) result.getData()) {
                            children.add(new RepoContentNode(model));
                        }
                        callback.setElements(children);
                    }
                }, variant);
            }
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            //do nothing
        }

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
