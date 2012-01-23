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

import java.util.HashMap;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.nexus.ext.gwt.ui.client.data.ResponseHandler;

public class ServerInstance {
    
    private ServerType serverType;
    
    private String id;

    private String name;
    
    private HashMap<String, String> defaultHeaders = new HashMap<String, String>();
    
    public ServerInstance(ServerType serverType) {
        if (serverType == null) {
            throw new NullPointerException("serverType is null");
        }
        this.serverType = serverType;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void addDefaultHeader(String name, String value) {
        defaultHeaders.put(name, value);
    }
    
    public void removeDefaultHeader(String name) {
        defaultHeaders.remove(name);
    }
    
    public String getServicePath() {
        return serverType.getServicePath() + "/" + getId();
    }
    
    public Resource getResource(String url) {
        String resourcePath = Constants.HOST + getServicePath() + "/" + url;
        resourcePath += (url.indexOf('?') == -1) ? "?" : "&";
        resourcePath += "_dc=" + System.currentTimeMillis();
        
        Resource resource = new DefaultResource(resourcePath);
        
        resource.addHeaders(defaultHeaders);
        
        return resource;
    }
    
    public void checkLogin(String authorizationToken, ResponseHandler handler) {
    }
    
    public void login(String username, String password, ResponseHandler handler) {
    }
    
    public void logout(ResponseHandler handler) {
    }
    
}
