package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.Action;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.SelectionProvider;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.tree.Tree;

class ContextMenuProvider implements Listener<BaseEvent> {
    
    private Component component;
    
    private SelectionProvider selectionProvider;
    
    private List<Action> actions = new ArrayList<Action>();
    
    public ContextMenuProvider(Component component, SelectionProvider selectionProvider) {
        this.component = component;
        this.selectionProvider = selectionProvider;
        
        setContextMenu(new Menu());
        component.addListener(Events.ContextMenu, this);
    }
    
    public void addAction(Action action) {
        actions.add(action);
    }

    public void handleEvent(BaseEvent be) {
        List selection = selectionProvider.getSelection();
        
        if (!selection.isEmpty()) {
            final Object data = selection.get(0);
            Menu menu = new Menu();
            
            for (final Action action : actions) {
                if (action.supports(data)) {
                    MenuItem item = new MenuItem(action.getCaption());
                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
                        public void componentSelected(MenuEvent event) {
                            action.execute(data);
                        }
                    });
                    menu.add(item);
                }
            }
            
            setContextMenu(menu);
        }
    }
    
    private void setContextMenu(Menu menu) {
        if (component instanceof Tree) {
            ((Tree) component).setContextMenu(menu);
        }
        else if (component instanceof Table) {
            ((Table) component).setContextMenu(menu);
        }
    }
    
}