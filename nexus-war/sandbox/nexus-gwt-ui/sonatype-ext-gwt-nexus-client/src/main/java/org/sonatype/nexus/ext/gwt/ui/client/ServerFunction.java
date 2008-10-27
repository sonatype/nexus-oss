package org.sonatype.nexus.ext.gwt.ui.client;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;

public class ServerFunction {
    
    private String menuName;
    
    private String tabName;
    
    private Container<Component> panel;
    private boolean panelInitialized = false;

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public Container<Component> getPanel(ServerInstance serverInstance) {
    	if (!panelInitialized && panel instanceof ServerFunctionPanel) {
    		((ServerFunctionPanel) panel).init(serverInstance);
    		panelInitialized = true;
    	}
        return panel;
    }

    public void setPanel(Container<Component> panel) {
        this.panel = panel;
    }

}
