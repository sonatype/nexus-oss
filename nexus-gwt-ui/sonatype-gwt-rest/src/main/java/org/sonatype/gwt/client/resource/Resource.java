package org.sonatype.gwt.client.resource;

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
     * Retuens the RESTRequestBuilder used by this resource.
     * 
     * @return
     */
    RESTRequestBuilder getRestRequestBuilder();

}
