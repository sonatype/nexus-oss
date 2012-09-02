/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.rest;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Support for {@link PlexusResource} implementations.
 *
 * @since 2.2
 */
public abstract class ResourceSupport
    extends AbstractNexusPlexusResource
{

    protected final Logger log = LoggerFactory.getLogger( getClass() );

    private String resourceUri;

    protected ResourceSupport()
    {
        setAvailable( true );
        setReadable( true );
        setModifiable( true );
        setNegotiateContent( true );
    }

    @Override
    public String getResourceUri()
    {
        if ( resourceUri == null )
        {
            Path path = getClass().getAnnotation( Path.class );
            if ( path != null )
            {
                resourceUri = path.value();
            }
        }
        return resourceUri;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

}