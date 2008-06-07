package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.Action;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.table.RowSelectionModel;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.user.client.Window;

public class RepoMaintenancePage extends LayoutContainer implements ServerFunctionPanel {

    ContentPanel repoPanel;

    public void init(ServerInstance server) {
        setLayout(new BorderLayout());
        setWidth("100%");
        setHeight("100%");

        addRepoList(server);
        addRepoPanel();
    }

    private void addRepoList(ServerInstance server) {
        ContentPanel panel = new ContentPanel() {
            {
                setHeaderVisible(false);
                setLayout(new FitLayout());
            }
        };

        final Table table = new Table<RowSelectionModel>() {
            {
                setColumnModel(new TableColumnModel(
                    new TableColumn("name", "Repository", 175f),
                    new TableColumn("repoType", "Type", 50f),
                    new TableColumn("status", "Status", 200f),
                    new TableColumn("contentUri", "Repository Path", 1f) {
                        {
                            setRenderer(new CellRenderer() {
                                public String render(String property, Object value) {
                                    String path = (String) value;
                                    path = Constants.HOST + path.replace(Constants.SERVICE_REPOSITORIES, Constants.CONTENT_REPOSITORIES);
                                    return "<a href=\"" + path + "\">" + path + "</a>";
                                }
                            });
                        }
                    }
                ));
            }
        };

        final RepoTableBinding tableBinding = new RepoTableBinding(table, server);
        tableBinding.getBinder().addSelectionChangedListener(new SelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                addRepoBrowser(event.getSelectedItem());
            }
        });

        ToolBar toolBar = new ToolBar();
        TextToolItem refreshButton = new TextToolItem("Refresh", "st-icon-refresh") {
            {
                addSelectionListener(new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent ce) {
                        tableBinding.reload();
                        table.recalculate();
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
                Window.alert(getCaption());
            }
        });
        
        tableMenu.addAction(new Action<ModelData>("Re-Index") {
            public void execute(ModelData data) {
                Window.alert(getCaption());
            }
        });
        
        tableMenu.addAction(new Action<ModelData>("Block Proxy") {
            public boolean supports(ModelData data) {
                return data.get("repoType").equals("proxy");
            }
            public void execute(ModelData data) {
                Window.alert(getCaption());
            }
        });
        
        tableMenu.addAction(new Action<ModelData>("Put Out of Service") {
            public void execute(ModelData data) {
                Window.alert(getCaption());
            }
        });
    }

    private void addRepoPanel() {
        repoPanel = new ContentPanel() {
            {
                setHeading("Repository Information");
                setLayout(new FitLayout());
                addText("Select a repository to view it").setStyleName("st-little-padding");
            }
        };

        add(repoPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));
    }

    private void addRepoBrowser(final ModelData repo) {
        if (repo == null) {
            return;
        }

        ContentPanel outerPanel = new ContentPanel() {
            {
                setFrame(true);
                setHeaderVisible(false);
                setLayout(new FitLayout());
            }
        };

        ContentPanel panel = new ContentPanel() {
            {
                setId("st-repo-browser");
                setHeading(((String) repo.get("name")) + " Repository Content");
                setBodyBorder(true);
                setBorders(true);
                setScrollMode(Style.Scroll.AUTO);
                // TODO: Add an action to this button
                getHeader().addTool(new ToolButton("x-tool-refresh"));
            }
        };

        Tree tree = new Tree();
        RepoTreeBinding treeBinding = new RepoTreeBinding(tree) {
            {
                selectRepo((String) repo.get("name"),
                           (String) repo.get("contentUri") + "/content");
            }
        };
        panel.add(tree);
        outerPanel.add(panel);

        repoPanel.removeAll();
        repoPanel.add(outerPanel);
        repoPanel.layout();

        ContextMenuProvider treeMenu =
            new ContextMenuProvider(tree, treeBinding.getBinder());
        
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
    }

}
