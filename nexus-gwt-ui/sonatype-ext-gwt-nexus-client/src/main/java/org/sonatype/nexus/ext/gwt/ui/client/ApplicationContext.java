package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoServer;

import com.google.gwt.user.client.Cookies;

public class ApplicationContext {
    
    private static final ApplicationContext instance = new ApplicationContext();
    
    private List<ServerType> serverTypes = new ArrayList<ServerType>();
    
    private boolean userLoggedIn = false;
    private String userName;
    
    private ApplicationContext() {
        RepoServer repoServer = new RepoServer();
        repoServer.init();
        serverTypes.add(repoServer);
        init();
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
    
    public void login(String user, String authorizationToken) {
        userLoggedIn = true;
        userName = user;
        setCookie("username", user);
        setCookie("authToken", authorizationToken);
        getLocalRepoServer().addDefaultHeader(
                "Authorization", "NexusAuthToken " + authorizationToken);
    }
    
    public void logout() {
        userLoggedIn = false;
        removeCookie("username");
        removeCookie("authToken");
        getLocalRepoServer().removeDefaultHeader("Authorization");
    }
    
    public boolean isUserLoggedIn() {
        return userLoggedIn;
    }
    
    public String getUserName() {
        return userName;
    }

    private void init() {
    }

}
