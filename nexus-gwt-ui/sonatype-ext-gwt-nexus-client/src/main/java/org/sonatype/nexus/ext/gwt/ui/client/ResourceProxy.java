package org.sonatype.nexus.ext.gwt.ui.client;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;

import com.extjs.gxt.ui.client.data.DataCallback;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.LoadConfig;
import com.extjs.gxt.ui.client.data.LoadResult;
import com.extjs.gxt.ui.client.data.BaseLoadResult.FailedLoadResult;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class ResourceProxy<C extends LoadConfig> implements DataProxy<C> {
    
    private final Resource resource;
    
    private final Variant variant;
    
    public ResourceProxy(Resource resource) {
        this(resource, Variant.APPLICATION_XML);
    }

    public ResourceProxy(Resource resource, Variant variant) {
        this.resource = resource;
        this.variant = variant;
    }

    public void load(final DataReader reader, final LoadConfig loadConfig,
            final DataCallback callback) {
        
        resource.get(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                callback.setResult(new FailedLoadResult(exception));
            }
            public void onResponseReceived(Request request, Response response) {
                String text = response.getText();
                LoadResult<?> result = reader.read(loadConfig, text);
                callback.setResult(result);
            }
        }, variant);
    }

}
