package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.Action;
import org.sonatype.nexus.ext.gwt.ui.client.ApplicationContext;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.data.ResponseHandler;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.ContentListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.Repository;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationClientPermissions.Permissions;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
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
import com.extjs.gxt.ui.client.widget.table.TableSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public class RepoMaintenancePage extends LayoutContainer implements ServerFunctionPanel {

    private RepoServerInstance server;
    private ContentPanel repoPanel;
    private ContentPanel repoTree;
    private ContentPanel repoTreePanel;
    private RepoTreeBinding repoTreeBinding;

    public void init(ServerInstance server) {
        this.server = (RepoServerInstance) server;
        
        setLayout(new BorderLayout());
        setWidth("100%");
        setHeight("100%");

        addRepoList();
        addRepoPanel();

        createRepoTree();

        showRepoHelp();
    }
    
    private void addRepoList() {
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
                    new TableColumn("statusText", "Status", 200f) {
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
                
                // This disables showing the repository when right clicking
                setSelectionModel(new TableSelectionModel() {
                    protected void onContextMenu(ContainerEvent event) {
                    }
                });
            }
        };
        
        final RepoTableBinding tableBinding = new RepoTableBinding(table, server);
        tableBinding.getBinder().addSelectionChangedListener(new SelectionChangedListener<Repository>() {
            public void selectionChanged(SelectionChangedEvent<Repository> event) {
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
        
        final boolean editEnabled
                = ApplicationContext.instance().checkPermission("maintRepos", Permissions.EDIT);
        
        ContextMenuProvider tableMenu = new ContextMenuProvider(table);
        
        tableMenu.addAction(new Action<Repository>("View") {
            
            public void execute(Repository repo) {
                showRepoTree(repo);
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Clear Cache") {
            
            public boolean supports(Repository repo) {
                return editEnabled && !repo.getRepoType().equals("virtual");
            }
            
            public void execute(Repository repo) {
                server.clearRepositoryCache(repo.getId(), null, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not clear the repository's cache", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Re-Index") {
            
            public boolean supports(Repository repo) {
                return editEnabled;
            }

            public void execute(Repository repo) {
                server.reindexRepository(repo.getId(), null, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not re-index the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Rebuild Attributes") {
            
            public boolean supports(Repository repo) {
                return editEnabled;
            }
            
            public void execute(Repository repo) {
                server.rebuildRepositoryAttributes(repo.getId(), null, new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not rebuild attributes in the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Block Proxy") {
            
            public boolean supports(Repository repo) {
                return editEnabled &&
                       "proxy".equals(repo.getRepoType()) &&
                       "allow".equals(repo.getStatus().getProxyMode());
            }
            
            public void execute(Repository repo) {
                RepositoryStatusResource status = repo.getStatus().copy();
                status.setId(RepoServerUtil.toRepositoryId(repo.getResourceURI()));
                status.setProxyMode("blockedManual");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatusResource>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not update the proxy repository status to blocked", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatusResource status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Allow Proxy") {
            
            public boolean supports(Repository repo) {
                return editEnabled &&
                       "proxy".equals(repo.getRepoType()) &&
                       !"allow".equals(repo.getStatus().getProxyMode());
            }
            
            public void execute(Repository repo) {
                RepositoryStatusResource status = repo.getStatus().copy();
                status.setId(RepoServerUtil.toRepositoryId(repo.getResourceURI()));
                status.setProxyMode("allow");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatusResource>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not update the proxy repository status to allow", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatusResource status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });
        
        tableMenu.addAction(new Action<Repository>("Put Out of Service") {
            
            public boolean supports(Repository repo) {
                return editEnabled &&
                       "inService".equals(repo.getStatus().getLocalStatus());
            }
            
            public void execute(Repository repo) {
                RepositoryStatusResource status = repo.getStatus().copy();
                status.setId(RepoServerUtil.toRepositoryId(repo.getResourceURI()));
                status.setLocalStatus("outOfService");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatusResource>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not put the repository out of service", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatusResource status) {
                        tableBinding.updateRepoStatus(status);
                    }
                    
                });
            }
            
        });

        tableMenu.addAction(new Action<Repository>("Put in Service") {
            
            public boolean supports(Repository repo) {
                return editEnabled &&
                       "outOfService".equals(repo.getStatus().getLocalStatus());
            }
            
            public void execute(Repository repo) {
                RepositoryStatusResource status = repo.getStatus().copy();
                status.setId(RepoServerUtil.toRepositoryId(repo.getResourceURI()));
                status.setLocalStatus("inService");
                
                server.updateRepositoryStatus(status, new ResponseHandler<RepositoryStatusResource>() {
                    
                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not put the repository into service", null);
                    }
                    
                    public void onSuccess(Response response, RepositoryStatusResource status) {
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

        repoTreeBinding = new RepoTreeBinding(tree, server);

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

        final boolean editEnabled
                = ApplicationContext.instance().checkPermission("maintRepos", Permissions.EDIT);

        ContextMenuProvider treeMenu = new ContextMenuProvider(tree);
        
        treeMenu.addAction(new Action<ContentListResource>("Clear Cache") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled &&
                       !"virtual".equals(repoTreeBinding.getSelectedRepository().getRepoType());
            }
            
            public void execute(ContentListResource data) {
                String repositoryId = repoTreeBinding.getSelectedRepository().getId();
                
                server.clearRepositoryCache(repositoryId, data.getRelativePath(), new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not clear the repository's cache", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        treeMenu.addAction(new Action<ContentListResource>("Re-Index") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled;
            }
            
            public void execute(ContentListResource data) {
                String repositoryId = repoTreeBinding.getSelectedRepository().getId();
                
                server.reindexRepository(repositoryId, data.getRelativePath(), new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not re-index the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });
        
        treeMenu.addAction(new Action<ContentListResource>("Rebuild Attributes") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled;
            }
            
            public void execute(ContentListResource data) {
                String repositoryId = repoTreeBinding.getSelectedRepository().getId();
                
                server.rebuildRepositoryAttributes(repositoryId, data.getRelativePath(), new ResponseHandler() {

                    public void onError(Response response, Throwable error) {
                        MessageBox.alert("Error", "The server did not rebuild attributes in the repository", null);
                    }

                    public void onSuccess(Response response, Object entity) {
                        //do nothing
                    }
                    
                });
            }
            
        });

        treeMenu.addAction(new Action<ContentListResource>("Download From Remote") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled && data.isLeaf() &&
                       "proxy".equals(repoTreeBinding.getSelectedRepository().getRepoType());
            }
            
            public void execute(ContentListResource data) {
                String url = repoTreeBinding.
                    getSelectedRepository().getRemoteUri() + data.getRelativePath();
                Window.open(url, "_blank", "");
            }
            
        });

        treeMenu.addAction(new Action<ContentListResource>("Download") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled && data.isLeaf();
            }
            
            public void execute(ContentListResource data) {
                String url = Constants.HOST +
                    "/nexus/content/repositories/apache-snapshots" + data.getRelativePath();
                Window.open(url, "_blank", "");
            }
            
        });

        treeMenu.addAction(new Action<ContentListResource>("Delete") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled && data.getParent() != null;
            }
            
            public void execute(final ContentListResource data) {
                MessageBox.confirm("Delete Repository Item?", "Delete the selected file/folder?", new Listener<WindowEvent>() {

                    public void handleEvent(WindowEvent event) {
                        if (!event.buttonClicked.getItemId().equals("yes")) {
                            return;
                        }
                        
                        String repositoryId = repoTreeBinding.getSelectedRepository().getId();
                        
                        server.deleteRepositoryItem(repositoryId, data.getRelativePath(), new ResponseHandler() {

                            public void onError(Response response, Throwable error) {
                                MessageBox.alert("Error", "The server did not delete the file/folder from the repository", null);
                            }

                            public void onSuccess(Response response, Object entity) {
                                repoTreeBinding.reload();
                            }
                            
                        });
                    }
                    
                });
            }
            
        });

        treeMenu.addAction(new Action<ContentListResource>("View Remote") {
            
            public boolean supports(ContentListResource data) {
                return editEnabled &&
                       "proxy".equals(repoTreeBinding.getSelectedRepository().getRepoType()) &&
                       !data.isLeaf() && data.getParent() != null;
            }
            
            public void execute(ContentListResource data) {
                String url = repoTreeBinding.
                    getSelectedRepository().getRemoteUri() + data.getRelativePath();
                Window.open(url, "_blank", "");
            }
            
        });

        repoTreePanel.add(tree);
        repoTree.add(repoTreePanel);
    }

    private void showRepoTree(Repository repo) {
        if (repo == null) {
            return;
        }

        repoTreePanel.setHeading((String) repo.get("name") + " Repository Content");
        repoTreeBinding.selectRepository(repo);

        repoPanel.removeAll();
        repoPanel.add(repoTree);
    }

    private void showRepoHelp() {
        repoPanel.removeAll();
        repoPanel.addText("Select a repository to view it")
                 .setStyleName("st-little-padding");
    }
    
}
