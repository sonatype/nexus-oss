/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Default Nexus URL resolver. It does simple URL.startsWith() matching of the supplied URL with the Nexus registered
 * proxy repositories's remote URL. This should be made better, since with solution like this, we are loosing many good
 * Nexus features (grouping and route mappings for example).
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexusURLResolver
    extends AbstractLogEnabled
    implements NexusURLResolver, Contextualizable
{

    public static final String APPLICATION_PORT = "applicationPort";

    /**
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * @plexus.configuration default-value="localhost"
     */
    private String nexusHost;

    /**
     * @plexus.configuration default-value="8081"
     */
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
        Repository mappedRepository = null;

        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( repository.getRemoteUrl() != null
                && url.toString().toLowerCase().startsWith( repository.getRemoteUrl().toLowerCase() ) )
            {
                mappedRepository = repository;

                break;
            }
        }

        if ( mappedRepository != null )
        {
            try
            {
                String prefix = "/nexus/dav/repositories/" + mappedRepository.getId();

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
