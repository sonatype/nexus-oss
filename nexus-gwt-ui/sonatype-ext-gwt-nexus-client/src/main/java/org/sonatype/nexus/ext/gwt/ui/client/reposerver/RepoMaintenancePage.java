package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.table.RowSelectionModel;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;

public class RepoMaintenancePage extends LayoutContainer implements ServerFunctionPanel {
    
    public void init(ServerInstance server) {
        Tree contentTree = new Tree();
        
        final RepoTreeBinding treeBinding = new RepoTreeBinding(contentTree);
        
        ContentPanel repoTreePanel = new ContentPanel();
        repoTreePanel.setTitle("Repository Information");
        repoTreePanel.add(contentTree);
        
        ContentPanel repoListPanel = new ContentPanel();
        repoListPanel.setHeaderVisible(false);
        
        TableColumnModel cm = new TableColumnModel(
            new TableColumn("name", "Repository", 175f),
            new TableColumn("repoType", "Type", 50f),
            new TableColumn("sStatus", "Status", 200f),
            new TableColumn("contentUri", "Repository Path", 1f)
        );
        
        Table repoTable = new Table<RowSelectionModel>(cm);
        
        final RepoTableBinding tableBinding = new RepoTableBinding(repoTable, server);
        
        tableBinding.getBinder().addSelectionListener(new SelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                List<Model> selection = event.getSelection();
                if (selection.size() > 0) {
                    Model repo = selection.get(0);
                    if (repo != null) {
                        treeBinder.selectRepo((String) repo.get("name"),
                                (String) repo.get("contentUri") + "/content");
                    }
                }
            }
        });
        
        repoListPanel.add(repoTable);
        
        ToolBar toolBar = new ToolBar();
        TextToolItem refreshButton = new TextToolItem("Refresh");
        refreshButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                tableBinding.reload();
            }
        });
        toolBar.add(refreshButton);
        
        repoListPanel.setTopComponent(toolBar);
        
        add(repoListPanel);
        add(repoTreePanel);
    }
    
}
