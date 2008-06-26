package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

public class AuthenticationLoginResource {
    
    private String authToken;
    
    private AuthenticationClientPermissions clientPermissions;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public AuthenticationClientPermissions getClientPermissions() {
        return clientPermissions;
    }

    public void setClientPermissions(
            AuthenticationClientPermissions clientPermissions) {
        this.clientPermissions = clientPermissions;
    }

}
