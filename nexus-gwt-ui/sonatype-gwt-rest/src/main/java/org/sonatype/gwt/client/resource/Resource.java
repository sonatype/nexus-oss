package org.sonatype.gwt.client.resource;

import java.util.Map;

import org.sonatype.gwt.client.request.RESTRequestBuilder;

/**
 * A REST Resource.
 * 
 * @author cstamas
 */
public interface Resource
    extends Uniform
{

    /**
     * Returns the resource URI of this resources.
     * 
     * @return
     */
    String getPath();

    /**
     * Returns the parent of this resource.
     * 
     * @return
     */
    Resource getParent();

    /**
     * Returns the child of this resource.
     * 
     * @param id a single pathelem to form subpath of this resource
     * @return
     */
    Resource getChild( String id );

    /**
     * Returns a resource by relative or absolute path from this resource, depending on the input parameter.
     * 
     * @param path
     * @return
     */
    Resource getResource( String path );

    /**
     * Returns the RESTRequestBuilder used by this resource.
     * 
     * @return
     */
    RESTRequestBuilder getRestRequestBuilder();

    /**
     * Adds a header field to the request
     * 
     * @param name the name of the header
     * @param value the value of the header
     */
    void addHeader( String name, String value );

    /**
     * Merge the given headers to the existing ones. Existing entries will be
     * overwritten with the new values.
     * 
     * @param headers the headers to be added to the existing ones.
     */
    public void addHeaders( Map<String, String> headers );

    /**
     * Get all of the headers
     * 
     * @return the headers
     */
    Map<String, String> getHeaders();

}
