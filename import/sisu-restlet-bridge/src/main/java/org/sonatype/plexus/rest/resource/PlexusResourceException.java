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

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class PlexusResourceException
    extends ResourceException
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -7465134306020613153L;

    /**
     * The object that will be returned to the client.
     */
    private Object resultObject;

    public PlexusResourceException( int code, String name, String description, String uri, Throwable cause,
        Object resultObject )
    {
        super( code, name, description, uri, cause );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( int code, String name, String description, String uri, Object resultObject )
    {
        super( code, name, description, uri );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( int code, Throwable cause, Object resultObject )
    {
        super( code, cause );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( int code, Object resultObject )
    {
        super( code );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( Status status, String description, Throwable cause, Object resultObject )
    {
        super( status, description, cause );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( Status status, String description, Object resultObject )
    {
        super( status, description );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( Status status, Throwable cause, Object resultObject )
    {
        super( status, cause );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( Status status, Object resultObject )
    {
        super( status );
        this.resultObject = resultObject;
    }

    public PlexusResourceException( Throwable cause, Object resultObject )
    {
        super( cause );
        this.resultObject = resultObject;
    }

    public Object getResultObject()
    {
        return resultObject;
    }

}
