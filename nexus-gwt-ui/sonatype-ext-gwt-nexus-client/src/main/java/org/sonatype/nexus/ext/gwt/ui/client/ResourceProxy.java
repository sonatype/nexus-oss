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
