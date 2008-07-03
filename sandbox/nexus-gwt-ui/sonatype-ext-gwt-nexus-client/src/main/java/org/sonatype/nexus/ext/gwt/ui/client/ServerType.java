package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.List;

public interface ServerType {
    
    void init();
    
    String getName();
    
    String getServicePath();
    
    List<ServerInstance> getInstances();
    
    List<ServerFunctionGroup> getFunctionGroups();

}
