package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.data.ErrorResponseException;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.Repository;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusResource;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
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
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RepoTableBinding {

    private RepoServerInstance server;

    private ListLoader loader;

    private ListStore store;
    
    private TableBinder<ModelData> binder;
    
    public RepoTableBinding(final Table table, final RepoServerInstance server) {
        this.server = server;
        
        DataProxy proxy = new DataProxy() {

            public void load(DataReader reader, Object loadConfig, final AsyncCallback callback) {
                server.getRepositories(new ResponseHandler<List<RepositoryListResource>>() {

                    public void onError(Response response, Throwable error) {
                        if (error != null) {
                            callback.onFailure(error);
                        } else {
                            callback.onFailure(new ErrorResponseException(response));
                        }
                    }

                    public void onSuccess(Response response, List<RepositoryListResource> repositories) {
                        callback.onSuccess(repositories);
                    }
                    
                });
            }
            
        };
        
        loader = new BaseListLoader(proxy) {
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
    
    public void updateRepoStatus(RepositoryStatusResource status) {
        BaseModelData item = new BaseModelData();
        item.set("resourceURI", server.getPath() + "/repository_statuses/" + status.getId());
        
        Repository storeItem = (Repository) store.findModel(item);
        storeItem.setStatus(status);
        storeItem.set("statusText", convertStatusToString(status));
        store.update(storeItem);
        
        loadStatuses(false);
    }

    private void loadStatuses(boolean forceCheck) {
        server.getRepositoryStatuses(forceCheck, new ResponseHandler<List<RepositoryStatusListResource>>() {

            public void onError(Response response, Throwable error) {
                MessageBox.alert("Error", "Status retrieval failed!", null);
            }

            public void onSuccess(Response response, List<RepositoryStatusListResource> statuses) {
                for (RepositoryStatusListResource repoStatus : statuses) {
                    Repository storeItem = (Repository) store.findModel(repoStatus);
                    if (storeItem != null) {
                        repoStatus.getStatus().setRepoType(repoStatus.getRepoType());
                        storeItem.setStatus(repoStatus.getStatus());
                        storeItem.set("statusText", convertStatusToString(repoStatus.getStatus()));
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
            
        });
    }

    private String convertStatusToString(RepositoryStatusResource status) {
    	String localStatus = status.getLocalStatus();

    	String statusText = localStatus.equals("inService") ? "In Service" : "Out of Service";
        
        if (status.getRepoType().equals("proxy")) {
            String remoteStatus = status.getRemoteStatus();
            String proxyMode = status.getProxyMode();
            if (proxyMode.startsWith("blocked")) {
            	statusText += proxyMode.equals("blockedAuto")
            	       ?  " - Remote Automatically Blocked"
            	       :  " - Remote Manually Blocked";
            	statusText += remoteStatus.equals("available")
                       ?  " and Available"
                       :  " and Unavailable";
            } else {
            	if (localStatus.equals("inService")) {
            		if (!remoteStatus.equals("available")) {
            			statusText += remoteStatus.equals("unknown")
            				   ?  " - <i>checking remote...</i>"
                               :  " - Attempting to Proxy and Remote Unavailable";
            		}
            	} else {
            		statusText += remoteStatus.equals("available")
                           ?  " - Remote Available"
                           :  " - Remote Unavailable";
            	}
            }
        }
        
        return statusText;
    }

}
