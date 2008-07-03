package org.sonatype.nexus.ext.gwt.ui.client.data;

import com.google.gwt.http.client.Response;

public class ErrorResponseException extends Exception {
    
    private Response response;

    public ErrorResponseException(Response response) {
        this.response = response;
    }

    public ErrorResponseException(Response response, String message) {
        super(message);
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

}
