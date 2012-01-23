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
