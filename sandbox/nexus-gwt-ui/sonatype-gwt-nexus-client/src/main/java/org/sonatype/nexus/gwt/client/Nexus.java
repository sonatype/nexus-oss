/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.gwt.client.request.RESTRequestBuilder;
import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.gwt.client.nexus.DefaultNexusRestApi;

import com.google.gwt.core.client.GWT;

/**
 * The Nexus REST API root.
 * 
 * @author cstamas
 */
public class Nexus
    extends DefaultResource
{
    public static final String LOCAL_INSTANCE_NAME = "local";

    private Variant defaultVariant;

    private Map namedInstances = new HashMap();

    /**
     * Instantiate Nexus resource instance using <code>GWT.getHostPageBaseURL()</code>. This presumes that the GWT
     * UI is served from the Nexus web application root.
     */
    public Nexus()
    {
        super( GWT.getHostPageBaseURL() + "service" );
    }

    /**
     * Instantiate Nexus resource instance from custom URL.
     * 
     * @param url
     */
    public Nexus( String url )
    {
        super( url );
    }

    /**
     * Instantiate Nexus resource instance with existing RESTRequestBuilder and known path.
     * 
     * @param path
     * @param requestBuilder
     */
    public Nexus( String path, RESTRequestBuilder requestBuilder )
    {
        super( path, requestBuilder );
    }

    /**
     * Returns the Nexus local instance.
     * 
     * @return
     */
    public NexusRestApi getLocalInstance()
    {
        return getNamedInstance( LOCAL_INSTANCE_NAME );
    }

    /**
     * Returns a named Nexus instance.
     * 
     * @param name
     * @return
     */
    public NexusRestApi getNamedInstance( String name )
    {
        if ( !namedInstances.containsKey( name ) )
        {
            namedInstances.put( name, new DefaultNexusRestApi( this, name ) );
        }
        return (NexusRestApi) namedInstances.get( name );
    }

    /**
     * Returns the default Variant used to communicate to Nexus REST API.
     * 
     * @return
     */
    public Variant getDefaultVariant()
    {
        if ( defaultVariant == null )
        {
            defaultVariant = Variant.APPLICATION_JSON;
        }
        return defaultVariant;
    }

    /**
     * Sets the default Variant used to communicate to Nexus REST API.
     * 
     * @param defaultVariant
     */
    public void setDefaultVariant( Variant defaultVariant )
    {
        this.defaultVariant = defaultVariant;
    }

}
