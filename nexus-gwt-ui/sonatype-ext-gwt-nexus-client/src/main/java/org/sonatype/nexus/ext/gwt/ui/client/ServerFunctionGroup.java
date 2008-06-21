package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

public class ServerFunctionGroup {

	private List<ServerFunction> functions = new ArrayList<ServerFunction>();

	private String name;
	
    public void addFunction(ServerFunction function) {
        functions.add(function);
    }

    public List<ServerFunction> getFunctions() {
        return functions;
    }

    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
}
