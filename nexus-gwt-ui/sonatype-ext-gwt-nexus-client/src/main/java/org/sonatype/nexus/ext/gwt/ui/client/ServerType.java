package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.List;

public interface ServerType {
    
    void init();
    
    String getName();
    
    String getPath();
    
    List<ServerInstance> getInstances();
    
    List<ServerFunction> getFunctions();

}
