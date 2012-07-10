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

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.params.CoreProtocolPNames;
import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.internal.rest.AbstractNexusClientFactory;
import org.sonatype.nexus.client.internal.rest.NexusXStreamFactory;
import org.sonatype.nexus.client.internal.rest.XStreamXmlProvider;
import org.sonatype.nexus.client.internal.util.Template;
import org.sonatype.nexus.client.rest.ConnectionInfo;
import org.sonatype.nexus.client.rest.ProxyInfo;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.thoughtworks.xstream.XStream;

@Named
@Singleton
public class JerseyNexusClientFactory
    extends AbstractNexusClientFactory<JerseyNexusClient>
{

    public JerseyNexusClientFactory( final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories )
    {
        super( subsystemFactories );
    }

    public JerseyNexusClientFactory( final Condition connectionCondition,
                                     final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories )
    {
        super( connectionCondition, subsystemFactories );
    }

    @Inject
    @SuppressWarnings( { "unchecked" } )
    public JerseyNexusClientFactory( final Set<SubsystemFactory<?, JerseyNexusClient>> subsystemFactories )
    {
        super( subsystemFactories.toArray( new SubsystemFactory[subsystemFactories.size()] ) );
    }

    @Override
    protected JerseyNexusClient doCreateFor( final Condition connectionCondition,
                                             final SubsystemFactory<?, JerseyNexusClient>[] subsystemFactories,
                                             final ConnectionInfo connectionInfo )
    {
        final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        // we are java2java client, so we use XML instead of JSON, as
        // some current Nexus are one way only! So, we fix for XML
        final XStream xstream = new NexusXStreamFactory().createAndConfigureForXml();
        config.getSingletons().add( new XStreamXmlProvider( xstream, MediaType.APPLICATION_XML_TYPE ) );
        // set _real_ URL for baseUrl, and not a redirection (typically http instead of https)
        config.getProperties().put( ApacheHttpClient4Config.PROPERTY_FOLLOW_REDIRECTS, Boolean.FALSE );
        applyAuthenticationIfAny( connectionInfo, config );
        applyProxyIfAny( connectionInfo, config );

        final ApacheHttpClient4 client = ApacheHttpClient4.create( config );
        // set UA
        client.getClientHandler().getHttpClient().getParams().setParameter( CoreProtocolPNames.USER_AGENT,
                                                                            "Nexus-Client/1.0" );
        // we use XML for communication (unlike web browsers do, for which JSON makes more sense)
        return new JerseyNexusClient( connectionCondition, subsystemFactories, connectionInfo, xstream, client,
                                      MediaType.APPLICATION_XML_TYPE );
    }

    // ==

    protected void applyAuthenticationIfAny( final ConnectionInfo connectionInfo, ApacheHttpClient4Config config )
    {
        if ( connectionInfo.getAuthenticationInfo() != null )
        {
            if ( connectionInfo.getAuthenticationInfo() instanceof UsernamePasswordAuthenticationInfo )
            {
                final UsernamePasswordAuthenticationInfo upinfo =
                    (UsernamePasswordAuthenticationInfo) connectionInfo.getAuthenticationInfo();
                final CredentialsProvider credentialsProvider =
                    new org.apache.http.impl.client.BasicCredentialsProvider();
                credentialsProvider.setCredentials( AuthScope.ANY,
                                                    new UsernamePasswordCredentials( upinfo.getUsername(),
                                                                                     upinfo.getPassword() ) );
                config.getProperties().put( ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                                            credentialsProvider );
                config.getProperties().put( ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, true );
            }
            else
            {
                throw new IllegalArgumentException( Template.of( "AuthenticationInfo of type %s is not supported!",
                                                                 connectionInfo.getAuthenticationInfo().getClass().getName() ).toString() );
            }
        }
    }

    protected void applyProxyIfAny( final ConnectionInfo connectionInfo, ApacheHttpClient4Config config )
    {
        if ( connectionInfo.getProxyInfos().size() > 0 )
        {
            final ProxyInfo proxyInfo = connectionInfo.getProxyInfos().get( connectionInfo.getBaseUrl().getProtocol() );
            if ( proxyInfo != null )
            {
                config.getProperties().put( ApacheHttpClient4Config.PROPERTY_PROXY_URI,
                                            "http://" + proxyInfo.getProxyHost() + ":" + proxyInfo.getProxyPort() );

                if ( proxyInfo.getProxyAuthentication() != null )
                {
                    if ( proxyInfo.getProxyAuthentication() instanceof UsernamePasswordAuthenticationInfo )
                    {
                        final UsernamePasswordAuthenticationInfo upinfo =
                            (UsernamePasswordAuthenticationInfo) connectionInfo.getAuthenticationInfo();
                        config.getProperties().put( ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME,
                                                    upinfo.getUsername() );
                        config.getProperties().put( ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD,
                                                    upinfo.getPassword() );
                    }
                    else
                    {
                        throw new IllegalArgumentException( Template.of(
                            "AuthenticationInfo of type %s is not supported!",
                            connectionInfo.getAuthenticationInfo().getClass().getName() ).toString() );
                    }
                }
            }
            else
            {
                throw new IllegalArgumentException( "ProxyInfo and BaseUrl protocols does not align!" );
            }
        }
    }
}
