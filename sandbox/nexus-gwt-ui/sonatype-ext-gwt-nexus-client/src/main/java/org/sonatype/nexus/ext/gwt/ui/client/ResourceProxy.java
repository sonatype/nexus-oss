/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.ext.gwt.ui.client;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;

import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ResourceProxy<C, D> implements DataProxy<C, D> {
    
    private final Resource resource;
    
    private final Variant variant;
    
    public ResourceProxy(Resource resource) {
        this(resource, Variant.APPLICATION_XML);
    }

    public ResourceProxy(Resource resource, Variant variant) {
        this.resource = resource;
        this.variant = variant;
    }

    public void load(final DataReader<C, D> reader, final C loadConfig,
            final AsyncCallback<D> callback) {
        
        resource.get(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                callback.onFailure(exception);
            }
            public void onResponseReceived(Request request, Response response) {
                try {
                    String text = response.getText();
                    D result;
                    if (reader != null) {
                        result = reader.read(loadConfig, text);
                    } else {
                        result = (D) text;
                    }
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        }, variant);
    }

}
