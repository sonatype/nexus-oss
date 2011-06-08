package org.sonatype.nexus.test.utils;

import com.thoughtworks.xstream.XStream;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Helper to make requests against Nexus and assert the response. Handy when used in Hamcrest Matcher style assertions.
 */
public class NexusRequest {

    private URL url;
    private Method method;
    // for debugging
    private Status status;
    private String responseText;
    private Throwable throwable;
    private Object resource;
    // custom XStream instance
    private XStream xStream;
    private String text = "";
    
    public NexusRequest(final String uriPart, final Method method, final Object resource) {
        Preconditions.checkNotNull(uriPart);
        Preconditions.checkNotNull(method);
        // Preconditions.checkNotNull(resource);
        final String completeURL = AbstractNexusIntegrationTest.nexusBaseUrl + uriPart;
        try {
            this.url = new URL(completeURL);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(completeURL + " is not a valid URL to send a request too", ex);
        }
        this.method = method;
        this.resource = resource;
    }
    
    public NexusRequest(final String uriPart, final Method method) {
        Preconditions.checkNotNull(uriPart);
        Preconditions.checkNotNull(method);
        final String completeURL = AbstractNexusIntegrationTest.nexusBaseUrl + uriPart;
        try {
            this.url = new URL(completeURL);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(completeURL + " is not a valid URL to send a request too", ex);
        }
        this.method = method;
    }
    
    public NexusRequest(final String uriPart, final Method method, final XStream customXStream) {
        Preconditions.checkNotNull(uriPart);
        Preconditions.checkNotNull(method);
        final String completeURL = AbstractNexusIntegrationTest.nexusBaseUrl + uriPart;
        try {
            this.url = new URL(completeURL);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(completeURL + " is not a valid URL to send a request too", ex);
        }
        this.method = method;
        this.xStream = customXStream;
    }

    
    private void captureResponseText(Response response) throws IOException {
        final Representation entity = response.getEntity();
        assertThat("Problem getting Entity for response text", entity, notNullValue());
        this.responseText = entity.getText();
    }

    public boolean assertStatusCode(final int expectedStatusCode){
        Response response = null;
        try {
            response = RequestFacade.sendMessage(url, method, XStreamUtil.toRepresentation(this.xStream, this.text, this.resource));
            this.status = response.getStatus();
            assertThat(this.status, notNullValue());
            if (this.status.getCode() != expectedStatusCode) {
                captureResponseText(response);
                return false;
            }
            return true;
        } catch (Throwable e){
            this.throwable = e;
            return false;
        } finally {
            RequestFacade.releaseResponse(response);
        }
    }
    
    public boolean assertSuccess(){
        Response response = null;
        try {
            response = RequestFacade.sendMessage(url, method, XStreamUtil.toRepresentation(this.xStream, this.text, this.resource));
            this.status = response.getStatus();
            assertThat(this.status, notNullValue());
            if (!this.status.isSuccess()) {
                captureResponseText(response);
                return false;
            }
            return true;
        } catch (Throwable e){
            this.throwable = e;
            return false;
        } finally {
            RequestFacade.releaseResponse(response);
        }
    }
    
    public Status getStatus() {
        return this.status;
    }
    
    public String getResponseText() {
        return this.responseText;
    }
    
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(status != null){
            str.append(status.toString()).append("\nfor ");
        }
        str.append(this.url.toString()).append(" ").append(this.method.getName());
        if(this.resource != null){
            str.append(" of ").append(this.resource.getClass());
        }
        return str.toString();
    }
    
}
