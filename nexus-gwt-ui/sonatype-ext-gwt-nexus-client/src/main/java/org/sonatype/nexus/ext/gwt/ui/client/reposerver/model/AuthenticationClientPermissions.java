package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class AuthenticationClientPermissions extends BaseModelData implements Entity {

    public String getType() {
        return "org.sonatype.nexus.rest.model.AuthenticationClientPermissions";
    }

    public Class getFieldType(String fieldName) {
        return Integer.class;
    }

    public Entity createEntity(String fieldName) {
        return null;
    }

}
