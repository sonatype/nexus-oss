/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
