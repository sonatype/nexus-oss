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
package org.sonatype.nexus.proxy.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Default Nexus URL resolver. It does simple URL.startsWith() matching of the supplied URL with the Nexus registered
 * proxy repositories's remote URL. This should be made better, since with solution like this, we are loosing many good
 * Nexus features (grouping and route mappings for example).
 * 
 * @author cstamas
 */
@Component(role=NexusURLResolver.class)
public class DefaultNexusURLResolver
    extends AbstractLoggingComponent
    implements NexusURLResolver, Contextualizable
{

    public static final String APPLICATION_PORT = "applicationPort";

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Configuration(value="localhost")
    private String nexusHost;

    @Configuration(value="8081")
    private int nexusPort;

    public void contextualize( Context ctx )
        throws ContextException
    {
        if ( ctx.contains( APPLICATION_PORT ) )
        {
            nexusPort = Integer.parseInt( (String) ctx.get( APPLICATION_PORT ) );
        }
    }

    public URL resolve( URL url )
    {
        ProxyRepository mappedRepository = null;

        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( repository instanceof ProxyRepository )
            {
                ProxyRepository proxy = (ProxyRepository) repository;

                if ( proxy.getRemoteUrl() != null
                    && url.toString().toLowerCase().startsWith( proxy.getRemoteUrl().toLowerCase() ) )
                {
                    mappedRepository = proxy;

                    break;
                }
            }
        }

        if ( mappedRepository != null )
        {
            try
            {
                String prefix = "/nexus/content/repositories/" + mappedRepository.getId();

                String repoPath = url.toString().substring( mappedRepository.getRemoteUrl().length() );

                if ( !repoPath.startsWith( "/" ) )
                {
                    repoPath = "/" + repoPath;
                }

                URL result = new URL( "HTTP", nexusHost, nexusPort, prefix + repoPath );

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "URLResolver: " + url.toString() + " -> " + result.toString() );
                }

                return result;
            }
            catch ( MalformedURLException e )
            {
                getLogger().error( "URLResolver: Cannot construct Nexus Repo URL: ", e );

                return null;
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "URLResolver: Cannot resolve " + url.toString() );
            }

            return null;
        }
    }
}
