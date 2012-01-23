/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
