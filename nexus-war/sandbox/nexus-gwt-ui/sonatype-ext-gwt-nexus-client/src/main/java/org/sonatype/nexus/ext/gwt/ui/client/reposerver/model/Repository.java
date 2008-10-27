package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Initializable;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoServerUtil;

public class Repository extends RepositoryListResource implements Initializable {
    
    public void initialize() {
        setId(RepoServerUtil.toRepositoryId(getResourceURI()));
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id", id);
    }

    public RepositoryStatusResource getStatus() {
        return get("status");
    }

    public void setStatus(RepositoryStatusResource status) {
        set("status", status);
    }

}
