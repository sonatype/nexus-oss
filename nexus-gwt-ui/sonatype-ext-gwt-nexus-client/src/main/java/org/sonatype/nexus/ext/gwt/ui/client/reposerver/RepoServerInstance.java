package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatus;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusResponse;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class RepoServerInstance extends ServerInstance {
    
    private static final Variant VARIANT = Variant.APPLICATION_XML;

    public RepoServerInstance(RepoServer repoServer) {
        super(repoServer);
    }
    
    public void updateRepositoryStatus(RepositoryStatus status, final ResponseHandler<RepositoryStatus> handler) {
        String path = "repositories/" + status.getId() + "/status";
        String request = new RepositoryStatusResponse(status).toXML();
        getResource(path).put(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                handler.onError(null, exception);
            }
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_ACCEPTED
                        || response.getStatusCode() == Response.SC_OK) {
                    RepositoryStatusResponse entity = new RepositoryStatusResponse();
                    entity.fromXML(response.getText());
                    handler.onResponse(entity.getData());
                } else {
                    handler.onError(response, null);
                }
            }
        }, new Representation(VARIANT, request));
    }

}
