package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class ContentListResource extends BaseTreeModel implements Entity {
    
    String resourceUri;

    String relativePath;
    
    String text;
    
    boolean leaf;

    public String getType() {
        return "org.sonatype.nexus.rest.model.ContentListResource";
    }

    public Class getFieldType(String fieldName) {
        return "leaf".equals(fieldName) ? boolean.class : String.class;
    }

    public Entity createEntity(String fieldName) {
        return null;
    }

    public String getResourceUri() {
        return get("resourceUri");
    }

    public void setResourceUri(String resourceUri) {
        set("resourceUri", resourceUri);
    }

    public String getRelativePath() {
        return get("relativePath");
    }

    public void setRelativePath(String relativePath) {
        set("relativePath", relativePath);
    }

    public String getText() {
        return get("text");
    }

    public void setText(String text) {
        set("text", text);
    }

    public boolean isLeaf() {
        return (Boolean) get("leaf");
    }

    public void setLeaf(boolean leaf) {
        set("leaf", leaf);
    }

}
