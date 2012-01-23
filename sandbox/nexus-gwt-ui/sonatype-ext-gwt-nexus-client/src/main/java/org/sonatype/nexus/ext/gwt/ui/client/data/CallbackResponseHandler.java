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
package org.sonatype.nexus.ext.gwt.ui.client.data;

import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CallbackResponseHandler<M> implements ResponseHandler<M> {
    
    private final AsyncCallback callback;
    
    public CallbackResponseHandler(AsyncCallback callback) {
        this.callback = callback;
    }

    public void onError(Response response, Throwable error) {
        if (error != null) {
            callback.onFailure(error);
        } else {
            callback.onFailure(new ErrorResponseException(response));
        }
    }

    public void onSuccess(Response response, M entity) {
        callback.onSuccess(entity);
    }

}
