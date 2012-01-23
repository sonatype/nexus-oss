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
