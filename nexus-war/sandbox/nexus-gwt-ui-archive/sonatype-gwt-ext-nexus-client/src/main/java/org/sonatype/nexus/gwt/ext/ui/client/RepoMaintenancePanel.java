package org.sonatype.nexus.gwt.ext.ui.client;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Variant;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.JsonReader;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;

public class RepoMaintenancePanel extends Panel {
    
    private Store store;
    
    public RepoMaintenancePanel() {
        setTitle("Maintenance");
        
        RecordDef recordDef = new RecordDef(new FieldDef[]{
                new StringFieldDef("name"),
                new StringFieldDef("repoType"),
                new StringFieldDef("sStatus", "status.localStatus"),
                new StringFieldDef("contentUri", "resourceURI")
        });
        
        JsonReader reader = new JsonReader(recordDef);
        reader.setId("resourceURI");
        reader.setRoot("data");
        
        store = new Store(reader);
        
        ColumnModel cm = new ColumnModel(new ColumnConfig[]{
            new ColumnConfig("Repository", "name", 175),
            new ColumnConfig("Type", "repoType", 50),
            new ColumnConfig("Status", "sStatus", 200),
            new ColumnConfig("Repository Path", "contentUri", 250)
        });
        
        GridPanel repoGrid = new GridPanel(store, cm);
        repoGrid.setHeight(200);
        repoGrid.setLoadMask(true);
        add(repoGrid);
        
        refresh();
    }
    
    public void refresh() {
        DefaultResource repos =
            new DefaultResource("http://localhost:8081/nexus/service/local/repositories");
        
        repos.get(new RequestCallback() {
            public void onError(Request request, Throwable e) {
                Window.alert(e.getMessage());
            }
            public void onResponseReceived(Request request, Response response) {
                store.loadJsonData(response.getText(), false);
            }
        }, Variant.APPLICATION_JSON);
    }

}
