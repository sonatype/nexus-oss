package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.AbstractServerType;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunction;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionGroup;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

public class RepoServer extends AbstractServerType {

    public void init() {
        ServerInstance local = new ServerInstance(this);
        local.setId("local");
        local.setName("Local");
        addInstance(local);
        
        ServerFunctionGroup views = new ServerFunctionGroup();
        views.setName("Views");
        
        ServerFunction repoMaintenance = new ServerFunction();
        repoMaintenance.setMenuName("Repositories");
        repoMaintenance.setTabName("Maintenance");
        repoMaintenance.setPanel(new RepoMaintenancePage());
        views.addFunction(repoMaintenance);
        
        addFunctionGroup(views);
    }

    public String getName() {
        return "Nexus";
    }

    public String getPath() {
        return "/nexus/service";
    }
    
}
