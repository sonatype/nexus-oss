/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
