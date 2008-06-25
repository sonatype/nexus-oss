package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.Action;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatus;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public class RepoMaintenancePage extends LayoutContainer implements ServerFunctionPanel {

    private ContentPanel repoPanel;
    private ContentPanel repoTree;
    private ContentPanel repoTreePanel;
    private RepoTreeBinding repoTreeBinding;

    public void init(ServerInstance server) {
        setLayout(new BorderLayout());
        setWidth("100%");
        setHeight("100%");

        addRepoList((RepoServerInstance) server);
        addRepoPanel();

        createRepoTree();

        showRepoHelp();
    }
    
    private void addRepoList(final RepoServerInstance server) {
        ContentPanel panel = new ContentPanel() {
            {
                setHeaderVisible(false);
                setLayout(new FitLayout());
            }
        };

        Table table = new Table() {
            {
                setColumnModel(new TableColumnModel(
                    new TableColumn("name", "Repository", 175f),
                    new TableColumn("repoType", "Type", 50f),
                    new TableColumn("status", "Status", 200f) {
                    	{
                    		setRenderer(new CellRenderer() {
                    			public String render(Component item, String property, Object value) {
                    				if (value == null) {
                    					return "<i>retrieving</i>";
                    				}
                    				return (String) value;
                    			}
                    		});
                    	}
                    },
                    new TableColumn("resourceURI", "Repository Path", 1f) {
                        {
                            setRenderer(new CellRenderer() {
                                public String render(Component item, String property, Object value) {
                                    String path = (String) value;
                                    path = Constants.HOST + path.replace(Constants.SERVICE_REPOSITORIES, Constants.CONTENT_REPOSITORIES);
                                    return "<a href=\"" + path + "\" target=\"_blank\">" + path + "</a>";
                                }
                            });
                        }
                    }
                ));
            }
        };
        
        final RepoTableBinding tableBinding = new RepoTableBinding(table, server);
        tableBinding.getBinder().addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
            public void selectionChanged(SelectionChangedEvent event) {
                showRepoTree(event.getSelectedItem());
            }
        });

        ToolBar toolBar = new ToolBar();
        TextToolItem refreshButton = new TextToolItem("Refresh", "st-icon-refresh") {
            {
                addSelectionListener(new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        tableBinding.reload();
                        showRepoHelp();
                    }
                });
            }
        };
        toolBar.add(refreshButton);

        panel.setTopComponent(toolBar);
        panel.add(table);

        BorderLayoutData panelLayoutData = new BorderLayoutData(Style.LayoutRegion.NORTH) {
            {
                setSplit(true);
                setSize(200);
                setMinSize(150);
                setMaxSize(400);
            }
        };

        add(panel, panelLayoutData);
        
        ContextMenuProvider tableMenu =
            new ContextMenuProvider(table, tableBinding.getBinder());
        
        tableMenu.addAction(new Action<ModelData>("View") {
            
            public void execute(ModelData data) {
                Window.alert(getCaption());
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Clear Cache") {
            
            public boolean supports(ModelData data) {
                String repoType = (String) data.get("repoType");
                return repoType.equals("hosted") || repoType.equals("proxy");
            }
            
            public void execute(ModelData data) {
                String repositoryId = RepoServerUtil.getRepositoryId(data);
                
                server.clearRepositoryCache(repositoryId, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not clear the repository's cache", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Re-Index") {
            
            public void execute(ModelData data) {
                String repositoryId = RepoServerUtil.getRepositoryId(data);
                
                server.reindexRepository(repositoryId, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not re-index the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Rebuild Attributes") {
            
            public boolean supports(ModelData data) {
                return data.get("repoType").equals("hosted");
            }
            
            public void execute(ModelData data) {
                String repositoryId = RepoServerUtil.getRepositoryId(data);
                
                server.rebuildRepositoryAttributes(repositoryId, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not rebuild attributes in the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Block Proxy") {
            
            public boolean supports(ModelData data) {
                return "proxy".equals(data.get("repoType")) && "allow".equals(data.get("proxyMode"));
            }
            
            public void execute(ModelData data) {
                RepositoryStatus status = new RepositoryStatus(data);
                status.setProxyMode("blockedManual");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatus>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not update the proxy repository status to blocked", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatus status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Allow Proxy") {
            
            public boolean supports(ModelData data) {
                return "proxy".equals(data.get("repoType")) && !"allow".equals(data.get("proxyMode"));
            }
            
            public void execute(ModelData data) {
                RepositoryStatus status = new RepositoryStatus(data);
                status.setProxyMode("allow");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatus>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not update the proxy repository status to allow", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatus status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<ModelData>("Put Out of Service") {
            
            public boolean supports(ModelData data) {
                return "inService".equals(data.get("localStatus"));
            }
            
            public void execute(ModelData data) {
                RepositoryStatus status = new RepositoryStatus(data);
                status.setLocalStatus("outOfService");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatus>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not put the repository out of service", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatus status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });

        tableMenu.addAction(new Action<ModelData>("Put in Service") {
            
            public boolean supports(ModelData data) {
                return "outOfService".equals(data.get("localStatus"));
            }
            
            public void execute(ModelData data) {
                RepositoryStatus status = new RepositoryStatus(data);
                status.setLocalStatus("inService");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatus>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not put the repository into service", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatus status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });
        
        addListener(Events.Render, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                tableBinding.reload();
            }
        });
    }

    private void addRepoPanel() {
        repoPanel = new ContentPanel() {
            {
                setHeading("Repository Information");
                setLayout(new FitLayout());
                setLayoutOnChange(true);
            }
        };

        add(repoPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));
    }

    private void createRepoTree() {
        repoTree = new ContentPanel() {
            {
                setFrame(true);
                setHeaderVisible(false);
                setLayout(new FitLayout());
            }
        };

        Tree tree = new Tree() {
            {
                setItemIconStyle("tree-leaf");
            }
        };

        repoTreeBinding = new RepoTreeBinding(tree);

        repoTreePanel = new ContentPanel() {
            {
                setId("st-repo-browser");
                setBodyBorder(true);
                setBorders(true);
                setScrollMode(Style.Scroll.AUTO);
                getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        repoTreeBinding.reload();
                    }
                }));
            }
        };

        ContextMenuProvider treeMenu =
            new ContextMenuProvider(tree, repoTreeBinding.getBinder());
        
        treeMenu.addAction(new Action<TreeModel>("Re-Index") {
            public void execute(TreeModel data) {
                Window.alert(getCaption());
            }
        });
        
        treeMenu.addAction(new Action<TreeModel>("Download") {
            public boolean supports(TreeModel data) {
                return data.isLeaf();
            }
            public void execute(TreeModel data) {
                Window.alert(getCaption());
            }
        });

        repoTreePanel.add(tree);
        repoTree.add(repoTreePanel);
    }

    private void showRepoTree(ModelData repo) {
        if (repo == null) {
            return;
        }

        repoTreePanel.setHeading((String) repo.get("name") + " Repository Content");
        repoTreeBinding.selectRepo(repo);

        repoPanel.removeAll();
        repoPanel.add(repoTree);
    }

    private void showRepoHelp() {
        repoPanel.removeAll();
        repoPanel.addText("Select a repository to view it")
                 .setStyleName("st-little-padding");
    }
    
}
