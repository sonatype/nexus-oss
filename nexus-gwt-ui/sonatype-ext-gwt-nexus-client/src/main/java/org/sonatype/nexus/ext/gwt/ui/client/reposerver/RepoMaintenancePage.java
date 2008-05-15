package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.viewer.SelectionChangedEvent;
import com.extjs.gxt.ui.client.viewer.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.table.RowSelectionModel;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.tree.Tree;

public class RepoMaintenancePage extends Container implements ServerFunctionPanel {
    
    public void init(ServerInstance server) {
        Tree contentTree = new Tree();
        
        final RepoTreeViewer treeViewer = new RepoTreeViewer(contentTree);
        
        ContentPanel repoTreePanel = new ContentPanel();
        repoTreePanel.setTitle("Repository Information");
        repoTreePanel.add(contentTree);
        
        ContentPanel repoListPanel = new ContentPanel();
        repoListPanel.setHeaderVisible(false);
        
        TableColumnModel cm = new TableColumnModel(
                new TableColumn("name", "Repository", 175f),
                new TableColumn("repoType", "Type", 50f),
                new TableColumn("sStatus", "Status", 200f),
                new TableColumn("contentUri", "Repository Path", 250f)
        );
        
        Table repoTable = new Table<RowSelectionModel>(cm);
        
        final RepoTableViewer tableViewer = new RepoTableViewer(repoTable, server);
        
        tableViewer.addSelectionListener(new SelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                Model repo = (Model) event.getSelection().getFirstElement();
                treeViewer.selectRepo((String) repo.get("name"),
                        (String) repo.get("contentUri") + "/content");
            }
        });
        
        repoListPanel.add(repoTable);
        
        ToolBar toolBar = new ToolBar();
        TextToolItem refreshButton = new TextToolItem("Refresh");
        refreshButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                tableViewer.reload();
            }
        });
        toolBar.add(refreshButton);
        
        repoListPanel.setTopComponent(toolBar);
        
        add(repoListPanel);
        add(repoTreePanel);
    }
    
}
