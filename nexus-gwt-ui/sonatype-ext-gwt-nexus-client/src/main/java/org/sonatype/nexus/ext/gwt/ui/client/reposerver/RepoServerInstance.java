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
        String url = "repositories/" + status.getId() + "/status";
        String request = new RepositoryStatusResponse(status).toXML();
        
        doPut(url, request, new ResponseProcessor(handler, Response.SC_OK, Response.SC_ACCEPTED) {
            
            protected Object createEntity(Response response) {
                RepositoryStatusResponse entity = new RepositoryStatusResponse();
                entity.fromXML(response.getText());
                return entity.getData();
            }
            
        });
    }
    
    public void reindexRepository(String repositoryId, final ResponseHandler handler) {
        String url = "data_index/repositories/" + repositoryId + "/content";
        getResource(url).delete(new ResponseProcessor(handler, Response.SC_OK));
    }
    
    public void clearRepositoryCache(String repositoryId, final ResponseHandler handler) {
        String url = "data_cache/repositories/" + repositoryId + "/content";
        getResource(url).delete(new ResponseProcessor(handler, Response.SC_OK));
    }
    
    public void rebuildRepositoryAttributes(String repositoryId, final ResponseHandler handler) {
        String url = "attributes/repositories/" + repositoryId + "/content";
        getResource(url).delete(new ResponseProcessor(handler, Response.SC_OK));
    }
    
    private void doPut(String path, String request, RequestCallback callback) {
        getResource(path).put(callback, new Representation(VARIANT, request));
    }

    
    private static class ResponseProcessor<E> implements RequestCallback {
        
        private ResponseHandler<E> handler;
        
        private int[] successCodes;
        
        public ResponseProcessor(ResponseHandler<E> handler, int... successCodes) {
            this.handler = handler;
            this.successCodes = successCodes;
        }

        public void onError(Request request, Throwable exception) {
            handler.onError(null, exception);
        }

        public void onResponseReceived(Request request, Response response) {
            for (int i = 0; i < successCodes.length; i++) {
                if (successCodes[i] == response.getStatusCode()) {
                    E entity = createEntity(response);
                    handler.onSuccess(response, entity);
                    return;
                }
            }
            
            handler.onError(response, null);
        }
        
        protected E createEntity(Response response) {
            return null;
        }
        
    }
    
}
