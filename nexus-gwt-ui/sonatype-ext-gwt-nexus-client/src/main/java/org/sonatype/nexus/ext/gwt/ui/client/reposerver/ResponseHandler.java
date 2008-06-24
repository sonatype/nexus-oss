package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import com.google.gwt.http.client.Response;

public interface ResponseHandler<E> {
    
    void onError(Response response, Throwable error);
    
    void onResponse(E entity);

}
