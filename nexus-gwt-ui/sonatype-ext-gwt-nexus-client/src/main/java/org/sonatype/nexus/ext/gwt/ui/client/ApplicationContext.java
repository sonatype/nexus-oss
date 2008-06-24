package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoServer;

import com.google.gwt.user.client.Cookies;

public class ApplicationContext {
    
    private static final ApplicationContext instance = new ApplicationContext();
    
    private List<ServerType> serverTypes = new ArrayList<ServerType>();
    
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
    
    private void init() {
    }

}
