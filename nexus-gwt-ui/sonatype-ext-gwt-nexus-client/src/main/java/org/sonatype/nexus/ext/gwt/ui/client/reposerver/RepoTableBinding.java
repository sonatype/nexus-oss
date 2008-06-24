package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.ResourceProxy;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatus;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.XmlReader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class RepoTableBinding {

    private static final ModelType DEFAULT_MODEL_TYPE = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.RepositoryListResource";
            addField("name");
            addField("repoType");
            addField("status");
            addField("resourceURI");
        }
    };

    private static final ModelType STATUSES_MODEL_TYPE = new ModelType() {
        {
            root = "data";
            recordName = "org.sonatype.nexus.rest.model.RepositoryStatusListResource";
            addField("resourceURI");
            addField("repoType");
            addField("status/localStatus");
            addField("status/remoteStatus");
            addField("status/proxyMode");
        }
    };
    
    private RepoServerInstance server;

    private ListLoader loader;

    private ListStore store;
    
    private TableBinder<ModelData> binder;
    
    public RepoTableBinding(Table table, ServerInstance server) {
        this(table, server, DEFAULT_MODEL_TYPE);
    }

    public RepoTableBinding(final Table table, final ServerInstance server, ModelType modelType) {
        this.server = (RepoServerInstance) server;
        
        DataReader reader = new XmlReader(modelType);
        Resource repoList = server.getResource("repositories");
        DataProxy proxy = new ResourceProxy(repoList, Variant.APPLICATION_XML);
        
        loader = new BaseListLoader(proxy , reader) {
            {
                setSortField("name");
                setSortDir(SortDir.ASC);
                addLoadListener(new LoadListener() {
                    public void loaderBeforeLoad(LoadEvent le) {
                        table.el().mask("Loading...");
                    }
                    public void loaderLoad(LoadEvent le) {
                        table.el().unmask();
                    }
                    public void loaderLoadException(LoadEvent le) {
                        table.el().mask("Loading failed!");
                    }
                });
            }
        };
        
        store = new ListStore(loader) {
            {
                addStoreListener(new StoreListener() {
                    public void storeDataChanged(StoreEvent event) {
                        loadStatuses(true);
                    }
                });
                setStoreSorter(new StoreSorter());
                setModelComparer(new ModelComparer() {
                	public boolean equals(ModelData model1, ModelData model2) {
                		String resourceURI1 = (String) model1.get("resourceURI");
                		String resourceURI2 = (String) model2.get("resourceURI");
                		resourceURI2 = resourceURI2.replace(server.getPath() + "/repository_statuses", server.getPath() + "/repositories");
                		return resourceURI1.equals(resourceURI2);
                	}
                });
            }
        };

        binder = new TableBinder(table, store);
    }
    
    public void reload() {
        int sortColumn = binder.getTable().getTableHeader().getSortColumn();
        if (sortColumn != -1) {
            loader.setSortField(binder.getTable().getColumn(sortColumn).getId());
            loader.setSortDir(binder.getTable().getColumn(sortColumn).getSortDir());
        }
        loader.load();
    }

    public TableBinder<ModelData> getBinder() {
        return binder;
    }
    
    public void updateRepoStatus(RepositoryStatus status) {
        //TODO: find a way to avoid ModelData/JavaBean mapping
        BaseModelData item = new BaseModelData();
        item.set("resourceURI", server.getPath() + "/repository_statuses/" + status.getId());
        item.set("status/localStatus", status.getLocalStatus());
        item.set("status/remoteStatus", status.getRemoteStatus());
        item.set("status/proxyMode", status.getProxyMode());
        
        ModelData storeItem = store.findModel(item);
        storeItem.set("localStatus", item.get("status/localStatus"));
        storeItem.set("status", convertStatusToString(item, storeItem));
        store.update(storeItem);
        
        loadStatuses(false);
    }

    private void loadStatuses(boolean forceCheck) {
        Resource resource = server.getResource("repository_statuses" + (forceCheck? "?forceCheck": ""));
        resource.get(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                MessageBox.alert("Error", "Status retrieval failed!", null);
            }
            public void onResponseReceived(Request request, Response response) {
                XmlReader reader = new XmlReader(STATUSES_MODEL_TYPE);
                ListLoadResult<ModelData> result = reader.read(null, response.getText());

                for (ModelData item : result.getData()) {
                	ModelData storeItem = store.findModel(item);
                	if (storeItem != null) {
                	    storeItem.set("localStatus", item.get("status/localStatus"));
                        storeItem.set("status", convertStatusToString(item, storeItem));
                        store.update(storeItem);
                	}
                }

                if (response.getStatusCode() == Response.SC_ACCEPTED) {
                	DelayedTask task = new DelayedTask(new Listener() {
                	    public void handleEvent(BaseEvent event) {
                	    	loadStatuses(false);
                	    }
                	});
                	task.delay(2000);
                }
            }
        }, Variant.APPLICATION_XML);
    }

    private String convertStatusToString(ModelData item, ModelData storeItem) {
    	String localStatus = item.get("status/localStatus");

    	String status = localStatus.equals("inService")? "In Service": "Out of Service";
        
        if (storeItem.get("repoType").equals("proxy")) {
            String remoteStatus = item.get("status/remoteStatus");
            String proxyMode = item.get("status/proxyMode");
            if (proxyMode.startsWith("blocked")) {
            	status += proxyMode.equals("blockedAuto")
            	       ?  " - Remote Automatically Blocked"
            	       :  " - Remote Manually Blocked";
            	status += remoteStatus.equals("available")
                       ?  " and Available"
                       :  " and Unavailable";
            } else {
            	if (localStatus.equals("inService")) {
            		if (!remoteStatus.equals("available")) {
            			status += remoteStatus.equals("unknown")
            				   ?  " - <i>checking remote...</i>"
                               :  " - Attempting to Proxy and Remote Unavailable";
            		}
            	} else {
            		status += remoteStatus.equals("available")
                           ?  " - Remote Available"
                           :  " - Remote Unavailable";
            	}
            }
        }
        
        return status;
    }

}
