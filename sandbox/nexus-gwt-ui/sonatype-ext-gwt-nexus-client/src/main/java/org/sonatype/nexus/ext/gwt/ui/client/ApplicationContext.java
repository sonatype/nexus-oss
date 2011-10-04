/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoServer;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationClientPermissions;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationLoginResource;

import com.google.gwt.user.client.Cookies;

public class ApplicationContext {
    
    private static final ApplicationContext instance = new ApplicationContext();
    
    private List<ServerType> serverTypes = new ArrayList<ServerType>();
    
    private String userName;
    private AuthenticationClientPermissions userPermissions;
    
    private ApplicationContext() {
        RepoServer repoServer = new RepoServer();
        repoServer.init();
        serverTypes.add(repoServer);
    }
    
    public static ApplicationContext instance() {
        return instance;
    }
    
    public List<ServerType> getServerTypes() {
        return serverTypes;
    }
    
    public ServerInstance getLocalRepoServer() {
        return serverTypes.get(0).getInstances().get(0);
    }
    
    public String getCookie(String name) {
        return Cookies.getCookie("st-" + name);
    }
    
    public void setCookie(String name, String value) {
        Date expires = new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 365));
        Cookies.setCookie("st-" + name, value, expires);
    }
    
    public void removeCookie(String name) {
        Cookies.removeCookie("st-" + name);
    }
    
    public void login(String name, AuthenticationLoginResource auth) {
        userName = name;
        userPermissions = auth.getClientPermissions();
        setCookie("username", userName);
        setCookie("authToken", auth.getAuthToken());
        getLocalRepoServer().addDefaultHeader(
                "Authorization", "NexusAuthToken " + auth.getAuthToken());
    }
    
    public void logout() {
        userName = null;
        userPermissions = AuthenticationClientPermissions.getAnonymousUserPermissions();
        removeCookie("username");
        removeCookie("authToken");
        getLocalRepoServer().removeDefaultHeader("Authorization");
    }

    public String getUserName() {
        return userName;
    }

    public boolean isUserLoggedIn() {
        return userName != null;
    }

    public boolean checkPermission(String name, Integer value) {
    	Integer permission = (Integer) userPermissions.get(name);
    	if (permission == null) {
    		return false;
    	}
        return (permission & value) == value;
    }
    
}
