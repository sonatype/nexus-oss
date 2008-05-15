package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServerType implements ServerType {

    private List<ServerFunction> functions = new ArrayList<ServerFunction>();
    
    private List<ServerInstance> instances = new ArrayList<ServerInstance>();
    
    public List<ServerFunction> getFunctions() {
        return functions;
    }

    public List<ServerInstance> getInstances() {
        return instances;
    }
    
    protected void addInstance(ServerInstance instance) {
        instances.add(instance);
    }
    
    protected void addFunction(ServerFunction function) {
        functions.add(function);
    }

}
