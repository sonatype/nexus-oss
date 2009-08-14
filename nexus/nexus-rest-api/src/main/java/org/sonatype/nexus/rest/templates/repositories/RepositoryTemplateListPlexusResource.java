/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.templates.repositories;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTemplateListPlexusResource" )
public class RepositoryTemplateListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/templates/repositories";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repotemplates]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryListResourceResponse result = new RepositoryListResourceResponse();

        RepositoryListResource repoRes;

        List<RepositoryTemplate> repoTemplates = getNexus().getRepositoryTemplates();

        for ( RepositoryTemplate template : repoTemplates )
        {
            repoRes = new RepositoryListResource();

            repoRes.setResourceURI( createChildReference( request, this, template.getId() ).toString() );

            repoRes.setId( template.getId() );

            repoRes.setName( template.getDescription() );

            if ( ProxyRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "proxy" );
            }
            else if ( HostedRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "hosted" );
            }
            else if ( ShadowRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "virtual" );
            }
            else if ( GroupRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "group" );
            }
            else
            {
                // huh?
                repoRes.setRepoType( template.getMainFacet().getName() );
            }

            // policy
            // another hack
            if ( template.getCoreConfiguration().getExternalConfiguration().getConfiguration( false ) instanceof AbstractMavenRepositoryConfiguration )
            {
                repoRes.setRepoPolicy( ( (AbstractMavenRepositoryConfiguration) template
                    .getCoreConfiguration().getExternalConfiguration().getConfiguration( false ) )
                    .getRepositoryPolicy().toString() );
            }

            // format
            repoRes.setFormat( template.getContentClass().getId() );

            // userManaged
            repoRes.setUserManaged( template.getConfigurableRepository().isUserManaged() );

            // exposed
            repoRes.setExposed( template.getConfigurableRepository().isExposed() );

            // ==
            // below are not used for templates (and does not make any sense)
            // effectiveLocalStorageUrl
            // remoteUri

            result.addData( repoRes );
        }

        return result;
    }
}