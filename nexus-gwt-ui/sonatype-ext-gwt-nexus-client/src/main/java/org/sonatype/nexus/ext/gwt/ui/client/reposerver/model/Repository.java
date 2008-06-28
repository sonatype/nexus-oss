package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

public class Repository extends RepositoryListResource {
    
    public RepositoryStatusResource getStatus() {
        return get("status");
    }

    public void setStatus(RepositoryStatusResource status) {
        set("status", status);
    }

}
