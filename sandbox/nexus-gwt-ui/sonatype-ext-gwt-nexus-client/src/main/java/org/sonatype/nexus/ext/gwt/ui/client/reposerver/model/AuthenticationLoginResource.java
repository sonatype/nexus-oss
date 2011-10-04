/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ext.gwt.ui.client.reposerver.model;

import org.sonatype.nexus.ext.gwt.ui.client.data.Entity;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class AuthenticationLoginResource extends BaseModelData implements Entity {
    
    public String getType() {
        return "org.sonatype.nexus.rest.model.AuthenticationLoginResource";
    }

    public Class getFieldType(String fieldName) {
        return "clientPermissions".equals(fieldName) ? AuthenticationClientPermissions.class : String.class;
    }

    public Entity createEntity(String fieldName) {
        return "clientPermissions".equals(fieldName) ? new AuthenticationClientPermissions() : null;
    }

    public String getAuthToken() {
        return get("authToken");
    }

    public void setAuthToken(String authToken) {
        set("authToken", authToken);
    }

    public AuthenticationClientPermissions getClientPermissions() {
        return get("clientPermissions");
    }

    public void setClientPermissions(
            AuthenticationClientPermissions clientPermissions) {
        set("clientPermissions", clientPermissions);
    }

}
