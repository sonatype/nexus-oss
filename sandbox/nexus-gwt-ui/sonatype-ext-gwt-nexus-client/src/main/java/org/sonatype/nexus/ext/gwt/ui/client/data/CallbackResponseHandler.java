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
