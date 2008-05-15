package org.sonatype.nexus.ext.gwt.ui.client;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;

public class ServerFunction {
    
    private String menuName;
    
    private String tabName;
    
    private String groupName;
    
    private Container<Component> panel;

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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Container<Component> getPanel() {
        return panel;
    }

    public void setPanel(Container<Component> panel) {
        this.panel = panel;
    }

}
