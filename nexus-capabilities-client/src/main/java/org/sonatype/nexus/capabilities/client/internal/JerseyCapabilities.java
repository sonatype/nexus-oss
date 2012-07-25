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
package org.sonatype.nexus.capabilities.client.internal;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.capabilities.model.XStreamConfigurator;
import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Jersey based Capabilities Nexus Client Subsystem implementation.
 *
 * @since 2.1
 */
public class JerseyCapabilities
    extends SubsystemSupport<JerseyNexusClient>
    implements Capabilities
{

    public JerseyCapabilities( JerseyNexusClient nexusClient )
    {
        super( nexusClient );
        XStreamConfigurator.configureXStream( nexusClient.getXStream() );
    }

    @Override
    public List<CapabilityListItemResource> list()
    {
        return list( false );
    }

    @Override
    public List<CapabilityListItemResource> list( final boolean includeHidden )
    {
        final MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add( "includeHidden", String.valueOf( includeHidden ) );
        return getNexusClient().serviceResource( "capabilities", queryParams )
            .get( CapabilitiesListResponseResource.class )
            .getData();
    }

    @Override
    public CapabilityResource get( final String id )
    {
        return getNexusClient().serviceResource( "capabilities/" + id )
            .get( CapabilityResponseResource.class )
            .getData();
    }

    @Override
    public CapabilityListItemResource add( final CapabilityResource capability )
    {
        final CapabilityRequestResource envelope = new CapabilityRequestResource();
        envelope.setData( capability );
        return getNexusClient().serviceResource( "capabilities" )
            .post( CapabilityStatusResponseResource.class, envelope )
            .getData();
    }

    @Override
    public CapabilityListItemResource update( final CapabilityResource capability )
    {
        final CapabilityRequestResource envelope = new CapabilityRequestResource();
        envelope.setData( capability );
        return getNexusClient().serviceResource( "capabilities/" + capability.getId() )
            .put( CapabilityStatusResponseResource.class, envelope )
            .getData();
    }

    @Override
    public void delete( final String id )
    {
        getNexusClient().serviceResource( "capabilities/" + id )
            .delete();
    }

    @Override
    public CapabilityListItemResource enable( final String id )
    {
        final CapabilityResource capability = get( id );
        capability.setEnabled( true );
        return update( capability );
    }

    @Override
    public CapabilityListItemResource disable( final String id )
    {
        final CapabilityResource capability = get( id );
        capability.setEnabled( false );
        return update( capability );
    }

}
