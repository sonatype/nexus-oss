package org.sonatype.nexus.ext.gwt.ui.client.data;

import com.google.gwt.http.client.Response;

public interface ResponseHandler<E> {
    
    void onError(Response response, Throwable error);
    
    void onSuccess(Response response, E entity);

}
