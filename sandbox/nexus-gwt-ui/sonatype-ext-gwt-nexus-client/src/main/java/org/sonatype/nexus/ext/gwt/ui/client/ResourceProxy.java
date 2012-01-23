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
