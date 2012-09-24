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
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;

public class JerseyUsers
    extends SubsystemSupport<JerseyNexusClient>
    implements Users
{

    public JerseyUsers( JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public List<UserResource> list()
    {
        return getNexusClient().serviceResource( "users" ).get( UserListResourceResponse.class ).getData();
    }

    @Override
    public UserResource get( String id )
    {
        return getNexusClient().serviceResource( itemPath( id ) ).get( UserResourceResponse.class ).getData();
    }

    @Override
    public UserResource create( UserResource item )
    {
        final UserResourceRequest request = new UserResourceRequest();
        request.setData( item );
        return getNexusClient().serviceResource( "users" ).post( UserResourceResponse.class, request ).getData();
    }

    @Override
    public UserResource update( UserResource item )
    {
        final UserResourceRequest request = new UserResourceRequest();
        request.setData( item );
        return getNexusClient().serviceResource( itemPath( item.getUserId() ) ).post( UserResourceResponse.class,
            request ).getData();
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
            return "users/" + URLEncoder.encode( id, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( "Could not url-encode id: " + id, e );
        }
    }

}
