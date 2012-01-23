/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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