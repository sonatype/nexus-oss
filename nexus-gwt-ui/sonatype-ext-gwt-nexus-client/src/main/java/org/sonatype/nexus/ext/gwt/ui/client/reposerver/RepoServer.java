package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.AbstractServerType;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunction;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionGroup;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

public class RepoServer extends AbstractServerType {

    public void init() {
        ServerInstance local = new RepoServerInstance(this);
        local.setId("local");
        local.setName("Local");
        addInstance(local);
        
        ServerFunctionGroup viewsGroup = new ServerFunctionGroup();
        viewsGroup.setName("Views");
        
        ServerFunction repoMaintenance = new ServerFunction();
        repoMaintenance.setMenuName("Repositories");
        repoMaintenance.setTabName("Maintenance");
        repoMaintenance.setPanel(new RepoMaintenancePage());
        viewsGroup.addFunction(repoMaintenance);
        
        addFunctionGroup(viewsGroup);
    }

    public String getName() {
        return "Nexus";
    }

    public String getPath() {
        return "/nexus/service";
    }
    
}
