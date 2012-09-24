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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.security.Roles;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;

public class JerseyRoles
    extends SubsystemSupport<JerseyNexusClient>
    implements Roles
{

    public JerseyRoles( JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public List<RoleResource> list()
    {
        return getNexusClient().serviceResource( "roles" ).get( RoleListResourceResponse.class ).getData();
    }

    @Override
    public RoleResource get( String id )
    {
        return getNexusClient().serviceResource( itemPath( id ) ).get( RoleResourceResponse.class ).getData();
    }

    @Override
    public RoleResource create( RoleResource item )
    {
        final RoleResourceRequest request = new RoleResourceRequest();
        request.setData( item );
        return getNexusClient().serviceResource( "roles" ).post( RoleResourceResponse.class, request ).getData();
    }

    @Override
    public RoleResource update( RoleResource item )
    {
        final RoleResourceRequest request = new RoleResourceRequest();
        request.setData( item );
        return getNexusClient().serviceResource( itemPath( item.getId() ) ).post( RoleResourceResponse.class, request ).getData();
    }

    @Override
    public void delete( String id )
    {
        getNexusClient().serviceResource( itemPath( id ) ).delete();

    }

    private String itemPath( String id )
    {
        try
        {
            return "roles/" + URLEncoder.encode( id, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( "Could not url-encode id: " + id, e );
        }
    }

}

