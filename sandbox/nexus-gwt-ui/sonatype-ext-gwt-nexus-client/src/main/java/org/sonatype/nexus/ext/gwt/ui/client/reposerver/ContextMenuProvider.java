/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.Action;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TableEvent;
import com.extjs.gxt.ui.client.event.TreeEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.tree.Tree;

class ContextMenuProvider implements Listener<BaseEvent> {
    
    private Component component;
    
    private List<Action> actions = new ArrayList<Action>();
    
    public ContextMenuProvider(Component component) {
        this.component = component;
        
        setContextMenu(new Menu());
        component.addListener(Events.ContextMenu, this);
    }
    
    public void addAction(Action action) {
        actions.add(action);
    }

    public void handleEvent(BaseEvent event) {
        boolean result = false;
        
        if (event instanceof TableEvent && ((TableEvent) event).item != null) {
            result = createMenu(((TableEvent) event).item.getModel());
        }
        else if (event instanceof TreeEvent && ((TreeEvent) event).item != null) {
            result = createMenu(((TreeEvent) event).item.getModel());
        }
        
        event.doit = result;
    }
    
    private boolean createMenu(final ModelData data) {
        Menu menu = new Menu();
            
        for (final Action action : actions) {
            if (action.supports(data)) {
                MenuItem item = new MenuItem(action.getCaption());
                item.addSelectionListener(new SelectionListener<MenuEvent>() {
                    public void componentSelected(MenuEvent event) {
                        action.execute(data);
                    }
                });
                item.setEnabled(action.isEnabled());
                menu.add(item);
            }
        }
            
        if (menu.getItemCount() == 0) {
            return false;
        }
        
        setContextMenu(menu);
        
        return true;
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