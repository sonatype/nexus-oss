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
package org.sonatype.nexus.gwt.ui.client.repository;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;

import com.google.gwt.json.client.JSONArray;

/**
 *
 * @author barath
 */
public class RepositoriesDataStore extends JSONArrayDataStore {
    
    private JSONArrayDataStore proxy = new JSONArrayDataStore();
    
    private JSONArrayDataStore hosted = new JSONArrayDataStore();

    private JSONArrayDataStore virtual = new JSONArrayDataStore();

    
    public JSONArrayDataStore getProxy() {
        return proxy;
    }

    public JSONArrayDataStore getHosted() {
        return hosted;
    }

    public JSONArrayDataStore getVirtual() {
        return virtual;
    }

    public void setElements(JSONArray repos) {
        super.setElements(repos);
        
        List proxies = new ArrayList();
        List hosteds = new ArrayList();
        List virtuals = new ArrayList();
        
        for (int i = 0; i < repos.size(); i++) {
            String repoType = repos.get(i).isObject().get("repoType").isString().stringValue();
            if ("proxy".equals(repoType)) {
                proxies.add(repos.get(i));
            } else if ("hosted".equals(repoType)) {
                hosteds.add(repos.get(i));
            } else if ("virtual".equals(repoType)) {
                virtuals.add(repos.get(i));
            }
        }
        
        proxy.setElements(proxies);
        hosted.setElements(hosteds);
        virtual.setElements(virtuals);
    }

    public void setElements(List elements) {
        super.setElements(elements);
        //TODO: implement
        throw new UnsupportedOperationException("not implemented yet");
    }
    
}
