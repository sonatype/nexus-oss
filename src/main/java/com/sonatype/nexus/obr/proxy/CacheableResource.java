/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.proxy;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;
import org.osgi.service.obr.Capability;
import org.osgi.service.obr.Repository;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resource;

/**
 * Simple wrapper around a remote bundle {@link Resource} that allows caching.
 */
public class CacheableResource
    implements Resource
{
    /**
     * The {@link Resource} property used to cache the remote URL.
     */
    public static final String REMOTE_URL = "remote-url";

    /**
     * The repository directory used to cache remote OSGi bundles.
     */
    public static final String BUNDLES_PATH = "/bundles/";

    Resource resource;

    Map<?, ?> properties;

    URL url;

    /**
     * Wrap the given {@link Resource} and redirect the URL to allow caching.
     * 
     * @param r the backing resource
     */
    public CacheableResource( Resource r )
    {
        // defaults
        resource = r;
        properties = r.getProperties();
        url = r.getURL();

        try
        {
            // store the original URL as a resource property
            Map<Object, Object> p = new HashMap<Object, Object>();
            p.putAll( (Map<?, ?>) r.getProperties() );
            p.put( REMOTE_URL, r.getURL().toExternalForm() );
            properties = p;

            // construct a new URL using the bundle's identity, making sure we encode spaces
            String id = URLEncoder.encode( r.getSymbolicName() + '-' + r.getVersion(), "UTF-8" );
            url = new URL( "file:" + BUNDLES_PATH + id + ".jar" );
        }
        catch ( IOException e )
        {
            // shouldn't happen, but if it does the defaults will be used
        }
    }

    public String getId()
    {
        return resource.getId();
    }

    public String getPresentationName()
    {
        return resource.getPresentationName();
    }

    public String getSymbolicName()
    {
        return resource.getSymbolicName();
    }

    public Version getVersion()
    {
        return resource.getVersion();
    }

    public URL getURL()
    {
        return url;
    }

    public String[] getCategories()
    {
        return resource.getCategories();
    }

    public Repository getRepository()
    {
        return resource.getRepository();
    }

    public Map<?, ?> getProperties()
    {
        return properties;
    }

    public Capability[] getCapabilities()
    {
        return resource.getCapabilities();
    }

    public Requirement[] getRequirements()
    {
        return resource.getRequirements();
    }
}
