package org.sonatype.gwt.client.resource;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;

/**
 * A uniform async interface.
 * 
 * @author cstamas
 */
public interface Uniform
{

    /**
     * Performs a GET against the resources. The expected Representation MIME type should be set by passed in Variant.
     * 
     * @param callback
     * @param variant
     * @return
     */
    Request get( RequestCallback callback, Variant variant );

    /**
     * Performs a HEAD against the resources. The expected Representation MIME type should be set by passed in Variant.
     * Note: this API is prepared to "misuse" the original HTTP HEAD and receive entities even if the RFC says no-no to
     * this.
     * 
     * @param callback
     * @param variant
     * @return
     */
    Request head( RequestCallback callback, Variant variant );

    /**
     * Performs a PUT against the resource. The posted Representation defined the MIME type too, hence an optional
     * response will be returned with same MIME type.
     * 
     * @param callback
     * @param variant
     * @return
     */
    Request put( RequestCallback callback, Representation representation );

    /**
     * Performs a POST against the resource. The posted Representation defined the MIME type too, hence an optional
     * response will be returned with same MIME type.
     * 
     * @param callback
     * @param variant
     * @return
     */
    Request post( RequestCallback callback, Representation representation );

    /**
     * Performs a DELETE against the resource. The DELETE has no returning entity, hence only status can be checked.
     * 
     * @param callback
     * @param variant
     * @return
     */
    Request delete( RequestCallback callback );

}
