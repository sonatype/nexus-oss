package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoServer;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class ApplicationContext {
    
    private static final ApplicationContext intance = new ApplicationContext();
    
    private List<ServerType> serverTypes = new ArrayList<ServerType>();
    
    private String serverState;
    
    private String serverVersion;
    
    private ApplicationContext() {
        RepoServer repoServer = new RepoServer();
        repoServer.init();
        serverTypes.add(repoServer);
        init();
    }
    
    public static ApplicationContext instance() {
        return intance;
    }
    
    public String getServerState() {
        return serverState;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public List<ServerType> getServerTypes() {
        return serverTypes;
    }
    
    public ServerInstance getLocalRepoServer() {
        return serverTypes.get(0).getInstances().get(0);
    }
    
    private void init() {
        getLocalRepoServer().getResource("status").get(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                serverState = "UNREACHABLE";
            }
            public void onResponseReceived(Request request, Response response) {
                Document doc = XMLParser.parse(response.getText());
                serverState = doc.getElementsByTagName("state").item(0).getFirstChild().getNodeValue();
                serverVersion = doc.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
            }
        }, Variant.APPLICATION_XML);
    }

}
