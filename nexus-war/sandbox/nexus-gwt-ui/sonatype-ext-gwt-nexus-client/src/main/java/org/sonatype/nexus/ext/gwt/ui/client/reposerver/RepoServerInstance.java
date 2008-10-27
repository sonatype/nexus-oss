package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.List;

import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.ext.gwt.ui.client.Constants;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.Util;
import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;
import org.sonatype.nexus.ext.gwt.ui.client.data.EntityFactory;
import org.sonatype.nexus.ext.gwt.ui.client.data.RepresentationParser;
import org.sonatype.nexus.ext.gwt.ui.client.data.ResponseHandler;
import org.sonatype.nexus.ext.gwt.ui.client.data.XMLRepresentationParser;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationLoginResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationClientPermissions;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.ContentListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.Repository;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusListResource;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.RepositoryStatusResource;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class RepoServerInstance extends ServerInstance {
    
    private static final Variant VARIANT = Variant.APPLICATION_XML;
    
    private final RepresentationParser parser = new XMLRepresentationParser();

    public RepoServerInstance(RepoServer repoServer) {
        super(repoServer);
    }
    
    public Variant getDefaultVariant() {
        return VARIANT;
    }
    
    public void getRepositories(
            final ResponseHandler<List<RepositoryListResource>> handler) {
        doGet("repositories/", new ResponseProcessor(handler) {
            
            protected Object createEntity(Response response) {
                return parser.parseEntityList(response.getText(), new EntityFactory() {
                    public Entity create() {
                        return new Repository();
                    }
                });
            }
            
        });
    }
    
    public void getRepositoryStatuses(boolean forceCheck,
            final ResponseHandler<List<RepositoryStatusListResource>> handler) {
        String url = "repository_statuses" + (forceCheck ? "?forceCheck" : "");
        doGet(url, new ResponseProcessor(handler, Response.SC_OK, Response.SC_ACCEPTED) {
            
            protected Object createEntity(Response response) {
                return parser.parseEntityList(response.getText(), new EntityFactory() {
                    public Entity create() {
                        return new RepositoryStatusListResource();
                    }
                });
            }
            
        });
    }
    
    public void updateRepositoryStatus(RepositoryStatusResource status,
            final ResponseHandler<RepositoryStatusResource> handler) {
        String url = "repositories/" + status.getId() + "/status";
        String request = parser.serializeEntity(
                "org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse", status);
        
        doPut(url, request, new ResponseProcessor(handler, Response.SC_OK, Response.SC_ACCEPTED) {
            
            protected Object createEntity(Response response) {
                return parser.parseEntity(response.getText(), new RepositoryStatusResource());
            }
            
        });
    }
    
    public void getRepositoryContent(ContentListResource parent,
            final ResponseHandler<List<ContentListResource>> handler) {
        int i = parent.getResourceUri().indexOf("repositories");
        String url = parent.getResourceUri().substring(i);
        
        doGet(url, new ResponseProcessor(handler) {
            
            protected Object createEntity(Response response) {
                return parser.parseEntityList(response.getText(), new EntityFactory() {
                    public Entity create() {
                        return new ContentListResource();
                    }
                });
            }
            
        });
    }
    
    public void deleteRepositoryItem(
            String repositoryId, String itemPath, final ResponseHandler handler) {
        String url = "repositories/" + repositoryId + "/content" + itemPath;
        getResource(url).delete(new ResponseProcessor(handler));
    }
    
    public void reindexRepository(
            String repositoryId, String path, final ResponseHandler handler) {
        String url = "data_index/repositories/" + repositoryId + "/content";
        if (path != null) {
            url += path;
        }
        getResource(url).delete(new ResponseProcessor(handler));
    }
    
    public void clearRepositoryCache(
            String repositoryId, String path, final ResponseHandler handler) {
        String url = "data_cache/repositories/" + repositoryId + "/content";
        if (path != null) {
            url += path;
        }
        getResource(url).delete(new ResponseProcessor(handler));
    }
    
    public void rebuildRepositoryAttributes(
            String repositoryId, String path, final ResponseHandler handler) {
        String url = "attributes/repositories/" + repositoryId + "/content";
        if (path != null) {
            url += path;
        }
        getResource(url).delete(new ResponseProcessor(handler));
    }
    
    public void doLogin(String authorization, ResponseHandler handler) {
        Resource resource = getResource(Constants.AUTHENTICATION_LOGIN);
        
        resource.addHeader("Authorization", authorization);

        resource.get(new ResponseProcessor(handler) {

            protected Object createEntity(Response response) {
                Document doc = XMLParser.parse(response.getText());
                
                AuthenticationLoginResource entity = new AuthenticationLoginResource();
                
                String authorizationToken = doc.getElementsByTagName("authToken").item(0).getFirstChild().getNodeValue();
                entity.setAuthToken(authorizationToken);
                
                AuthenticationClientPermissions clientPermissions
                        = new AuthenticationClientPermissions();
                NodeList nodes = doc.getElementsByTagName("clientPermissions").item(0).getChildNodes();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String value = node.getFirstChild().getNodeValue();
                        clientPermissions.set(node.getNodeName(), Integer.valueOf(value));
                    }
                }
                entity.setClientPermissions(clientPermissions);
                
                return entity;
            }

        }, VARIANT);
        
    }
    
    public void checkLogin(String authorizationToken, ResponseHandler handler) {
        doLogin("NexusAuthToken " + authorizationToken, handler);
    }
    
    public void login(String username, String password, ResponseHandler handler) {
        // TODO: Use real HTTP basic authentication
        doLogin("Basic " + Util.base64Encode(username + ":" + password), handler);
    }

    public void logout(final ResponseHandler handler) {
        doGet(Constants.AUTHENTICATION_LOGOUT, new ResponseProcessor(handler));
    }

    private void doGet(String path, RequestCallback callback) {
        getResource(path).get(callback, VARIANT);
    }
    
    private void doPut(String path, String request, RequestCallback callback) {
        getResource(path).put(callback, new Representation(VARIANT, request));
    }
    
    
    private static class ResponseProcessor<E> implements RequestCallback {
        
        private ResponseHandler<E> handler;
        
        private int[] successCodes;
        
        public ResponseProcessor(ResponseHandler<E> handler, int... successCodes) {
            this.handler = handler;
            
            if (successCodes.length > 0) {
                this.successCodes = successCodes;
            } else {
                this.successCodes = new int[] { Response.SC_OK };
            }
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
