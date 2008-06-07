package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.ResourceProxy;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.table.Table;

public class RepoTableBinding {

    private static final ModelType DEFAULT_MODEL_TYPE = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.RepositoryListResource";
            addField("name");
            addField("repoType");
            addField("status", "status/localStatus");
            addField("contentUri", "resourceURI");
        }
    };

    private ListLoader loader;
    
    private TableBinder<ModelData> binder;
    
    public RepoTableBinding(Table table, ServerInstance server) {
        this(table, server, DEFAULT_MODEL_TYPE);
    }

    public RepoTableBinding(final Table table, ServerInstance server, ModelType modelType) {
        DataReader reader = new XmlReader(modelType);
        Resource repoList = server.getResource("repositories");
        DataProxy proxy = new ResourceProxy(repoList, Variant.APPLICATION_XML);
        
        loader = new BaseListLoader(proxy , reader);
        loader.addLoadListener(new LoadListener() {
            public void loaderBeforeLoad(LoadEvent le) {
                table.el().mask("Loading...");
            }
            public void loaderLoad(LoadEvent le) {
                table.el().unmask();
            }
            public void loaderLoadException(LoadEvent le) {
                table.el().mask("Loading failed!!!!");
            }
        });
        
        binder = new TableBinder(table, new ListStore(loader));
        
        // FIXME: This doesn't work
        binder.getStore().setDefaultSort("name", SortDir.ASC);
    }
    
    public void reload() {
        loader.load();
    }

    public TableBinder<ModelData> getBinder() {
        return binder;
    }

}
