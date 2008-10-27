package org.sonatype.gwt.client.request;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.gwt.client.resource.Variant;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Builds GWT RequestBuilders to consume REST resources.
 * 
 * @author cstamas
 */
public interface RESTRequestBuilder
{
    RequestBuilder buildGet( Resource resource, Variant variant );

    RequestBuilder buildHead( Resource resource, Variant variant );

    RequestBuilder buildPut( Resource resource, Variant variant );

    RequestBuilder buildPost( Resource resource, Variant variant );

    RequestBuilder buildDelete( Resource resource );
}
