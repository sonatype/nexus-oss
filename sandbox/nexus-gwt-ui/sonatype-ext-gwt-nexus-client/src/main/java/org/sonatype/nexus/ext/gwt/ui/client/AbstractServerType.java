package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServerType implements ServerType {

    private List<ServerFunctionGroup> functionGroups = new ArrayList<ServerFunctionGroup>();
    
    private List<ServerInstance> instances = new ArrayList<ServerInstance>();
    
    public List<ServerFunctionGroup> getFunctionGroups() {
        return functionGroups;
    }

    public List<ServerInstance> getInstances() {
        return instances;
    }
    
    protected void addInstance(ServerInstance instance) {
        instances.add(instance);
    }
    
    protected void addFunctionGroup(ServerFunctionGroup functionGroup) {
        functionGroups.add(functionGroup);
    }

}
