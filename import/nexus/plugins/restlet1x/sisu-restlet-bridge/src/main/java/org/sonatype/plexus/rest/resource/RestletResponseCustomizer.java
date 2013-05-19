/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest.resource;

import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Optional interface to be implemented by {@link Representation}s that need to customize Restlet {@link Response} as
 * for example add additional HTTP headers.
 *
 * @since 1.20
 */
public interface RestletResponseCustomizer
{

    /**
     * Callback just before returning the response to Restlet framework. The call is always done after the call to
     * {@link PlexusResource} GET/POST/PUT/UPLOAD was performed.
     *
     * @param response Restlet response
     */
    void customize( Response response );

}
