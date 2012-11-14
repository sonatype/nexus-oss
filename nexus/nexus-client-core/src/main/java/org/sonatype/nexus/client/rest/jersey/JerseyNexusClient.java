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
package org.sonatype.nexus.client.rest.jersey;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.NexusErrorMessageException;
import org.sonatype.nexus.client.core.NexusStatus;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.internal.msg.ErrorMessage;
import org.sonatype.nexus.client.internal.msg.ErrorResponse;
import org.sonatype.nexus.client.internal.rest.AbstractXStreamNexusClient;
import org.sonatype.nexus.client.internal.util.Check;
import org.sonatype.nexus.client.rest.ConnectionInfo;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.thoughtworks.xstream.XStream;

/**
 * Jersey client with some extra fluff: it maintains reference to XStream used by Provider it uses, to make it able to
 * pass XStream around (toward subsystems) to apply needed XStream configuration. As Nexus currently is married to
 * XStream, this will probably change, hence, this class, as one of the implementations keeps the fact of XStream use
 * encapsulated, I did not want to proliferate it through all of Nexus Client. This class should not be instantiated
 * manually, use {@link JerseyNexusClientFactory} for it.
 *
 * @since 2.1
 */
public class JerseyNexusClient
    extends AbstractXStreamNexusClient
{

    private Client client;

    private final MediaType mediaType;

    private final LinkedHashMap<Class<?>, SubsystemFactory<?, JerseyNexusClient>> subsystemFactoryMap;

    public JerseyNexusClient( final Condition connectionCondition,
                              final SubsystemFactory<?, JerseyNexusClient>[] subsystemFactories,
                              final ConnectionInfo connectionInfo, final XStream xstream, final Client client,
                              final MediaType mediaType )
    {
        super( connectionInfo, xstream );
        this.client = Check.notNull( client, Client.class );
        this.mediaType = Check.notNull( mediaType, MediaType.class );
        this.subsystemFactoryMap = new LinkedHashMap<Class<?>, SubsystemFactory<?, JerseyNexusClient>>();
        getLogger().debug( "Client created for media-type {} and connection {}", mediaType, connectionInfo );
        initializeConnection( connectionCondition );
        initializeSubsystems( subsystemFactories );
    }

    public Client getClient()
    {
        return client;
    }

    public MediaType getMediaType()
    {
        return mediaType;
    }

    public String resolvePath( final String path )
    {
        // we need more logic here, but for now will do it ;)
        return getConnectionInfo().getBaseUrl() + path;
    }

    public String resolveServicePath( final String path )
    {
        // we need more logic here, but for now will do it ;)
        return resolvePath( "service/local/" + path );
    }

    public WebResource.Builder serviceResource( final String uri )
    {
        return getClient()
            .resource( resolveServicePath( uri ) )
            .type( getMediaType() )
            .accept( getMediaType() );
    }

    public WebResource.Builder serviceResource( final String uri, final MultivaluedMap<String, String> queryParameters )
    {
        return getClient()
            .resource( resolveServicePath( uri ) )
            .queryParams( queryParameters )
            .type( getMediaType() )
            .accept( getMediaType() );
    }

    public WebResource.Builder uri( final String uri )
    {
        return getClient()
            .resource( resolvePath( uri ) )
            .getRequestBuilder();
    }

    public WebResource.Builder uri( final String uri, final MultivaluedMap<String, String> queryParameters )
    {
        return getClient()
            .resource( resolvePath( uri ) )
            .queryParams( queryParameters )
            .getRequestBuilder();
    }

    @Override
    public NexusStatus getStatus()
    {
        final StatusResource response = serviceResource( "status" ).get( StatusResourceResponse.class ).getData();
        return new NexusStatus( response.getAppName(), response.getFormattedAppName(), response.getVersion(),
                                response.getApiVersion(), response.getEditionLong(), response.getEditionShort(),
                                response.getState(),
                                response.getInitializedAt(), response.getStartedAt(), response.getLastConfigChange(),
                                -1,
                                response.getBaseUrl() );
    }

    @Override
    public void close()
    {
        try
        {
            if ( client != null )
            {
                client.destroy();
                client = null;
            }
        }
        finally
        {
            super.close();
        }
    }

    // ==

    protected void initializeSubsystems( final SubsystemFactory<?, JerseyNexusClient>[] subsystemFactories )
    {
        Check.notNull( subsystemFactories, "Subsystem factories" );
        getLogger().debug( "Registering available subsystem factories: {} ", subsystemFactories );
        for ( SubsystemFactory<?, JerseyNexusClient> subsystemFactory : subsystemFactories )
        {
            subsystemFactoryMap.put( subsystemFactory.getType(), subsystemFactory );
        }
    }

    @Override
    protected Collection<Class<?>> getConfiguredSubsystemTypes()
    {
        return subsystemFactoryMap.keySet();
    }

    @Override
    protected <S> S createSubsystem( final Class<S> subsystemType )
        throws IllegalArgumentException
    {
        if ( subsystemFactoryMap.containsKey( subsystemType ) )
        {
            final SubsystemFactory<?, JerseyNexusClient> subsystemFactory = subsystemFactoryMap.get( subsystemType );
            if ( subsystemFactory.availableWhen().isSatisfiedBy( getNexusStatus() ) )
            {
                return subsystemType.cast( subsystemFactory.create( this ) );
            }
            else
            {
                throw new IllegalArgumentException( "Subsystem conditions not satisfied: "
                                                        + subsystemFactory.availableWhen().explainNotSatisfied(
                    getNexusStatus() ) );
            }
        }
        else
        {
            throw new IllegalArgumentException( "No SubsystemFactory configured for subsystem having type "
                                                    + subsystemType.getName() );
        }
    }

    // ==

    /**
     * Internal method to be used by subsystem implementations to convert Jersey specific exception to
     * {@link NexusErrorMessageException}.
     */
    public NexusErrorMessageException convertErrorResponse( final int statusCode, final String reasonPhrase,
                                                            final ErrorResponse errorResponse )
    {
        final Map<String, String> errors = new LinkedHashMap<String, String>();
        for ( ErrorMessage errorMessage : errorResponse.getErrors() )
        {
            errors.put( errorMessage.getId(), errorMessage.getMsg() );
        }

        return new NexusErrorMessageException( statusCode, reasonPhrase, errors );
    }
}
