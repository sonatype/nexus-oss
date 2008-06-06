package org.sonatype.nexus.ext.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public interface Constants {

    String HOST = GWT.isScript()? Window.Location.getProtocol() + "//" + Window.Location.getHost()
                                : "http://localhost:8081";
    String PATH = Window.Location.getPath().substring(0, Window.Location.getPath().lastIndexOf("/"));

    String SERVICE = PATH + "/service/local";
    String SERVICE_REPOSITORIES = SERVICE + "/repositories";

    String CONTENT = PATH + "/content";
    String CONTENT_REPOSITORIES = CONTENT + "/repositories";

}
