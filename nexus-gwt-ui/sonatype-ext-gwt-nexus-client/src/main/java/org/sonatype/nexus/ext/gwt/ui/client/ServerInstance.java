package org.sonatype.nexus.ext.gwt.ui.client;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Resource;

public class ServerInstance {
    
    private ServerType serverType;
    
    private String id;

    private String name;
    
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
    
    public String getPath() {
        return serverType.getPath() + "/" + getId();
    }
    
    public Resource getResource(String url) {
        String resporcePath = Constants.HOST + getPath() + "/" + url;
        resporcePath += (url.indexOf('?') == -1) ? "?" : "&";
        resporcePath += "_dc=" + System.currentTimeMillis();
        return new DefaultResource(resporcePath);
    }
    
}
