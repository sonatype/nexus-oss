package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.ResourceProxy;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.data.DataLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.viewer.ModelCellLabelProvider;
import com.extjs.gxt.ui.client.viewer.ModelContentProvider;
import com.extjs.gxt.ui.client.viewer.TableViewer;
import com.extjs.gxt.ui.client.widget.table.Table;

public class RepoTableViewer extends TableViewer {
    
    private ModelType modelType = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.RepositoryListResource";
            addField("name");
            addField("repoType");
            addField("sStatus", "status.localStatus");
            addField("contentUri", "resourceURI");
        }
    };

    private Loader loader;
    
    public RepoTableViewer(Table table, ServerInstance server, ModelType modelType) {
        this(table, server);
        this.modelType = modelType;
    }
    
    public RepoTableViewer(final Table table, ServerInstance server) {
        super(table);
        
        setContentProvider(new ModelContentProvider());
        
        ModelCellLabelProvider lp = new ModelCellLabelProvider();
        for (int i = 0; i < table.getColumnCount(); i++) {
            getViewerColumn(i).setLabelProvider(lp);
        }

        DataReader reader = new XmlReader(modelType);
        Resource repoList = server.getResource("repositories");
        DataProxy proxy = new ResourceProxy(repoList, Variant.APPLICATION_XML);
        loader = new DataLoader(proxy , reader);
        loader.addListener(Loader.Load, new Listener<LoadEvent>() {
            public void handleEvent(LoadEvent de) {
                setInput(de.result.getData());
                table.el.unmask();
            }
        });
        loader.addListener(Loader.LoadException, new Listener<LoadEvent>() {
            public void handleEvent(LoadEvent de) {
                table.el.mask("Loading failed!");
            }
        });
    }

    public void reload() {
        getTable().el.mask("Loading...");
        loader.load();
    }

}
