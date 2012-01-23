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
