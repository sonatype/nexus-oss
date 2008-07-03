package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class RepositoryListResource extends BaseModelData implements Entity {
    
    public String getType() {
        return "org.sonatype.nexus.rest.model.RepositoryListResource";
    }

    public Class getFieldType(String fieldName) {
        return String.class;
    }

    public Entity createEntity(String fieldName) {
        return null;
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getRepoType() {
        return get("repoType");
    }

    public void setRepoType(String repoType) {
        set("repoType", repoType);
    }

    public String getResourceURI() {
        return get("resourceURI");
    }

    public void setResourceURI(String resourceURI) {
        set("resourceURI", resourceURI);
    }

    public String getRemoteUri() {
        return get("remoteUri");
    }

    public void setRemoteUri(String remoteUri) {
        set("remoteUri", remoteUri);
    }

}
